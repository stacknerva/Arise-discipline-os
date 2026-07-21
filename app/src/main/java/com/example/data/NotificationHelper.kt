package com.example.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import com.example.R
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Calendar
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationHelper(private val context: Context) {
    
    init {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            createNotificationChannel()
        }
    }

    private fun getActiveChannelId(mode: String, uriStr: String?): String {
        return when (mode) {
            "silent" -> "arise_reminders_silent"
            "custom" -> "arise_reminders_custom_${uriStr?.hashCode() ?: "none"}"
            else -> "arise_reminders_default"
        }
    }

    private suspend fun getSoundSettings(): Pair<String, String?> {
        return try {
            val repository = SettingsRepository(context)
            val mode = repository.notificationSoundMode.first()
            val uri = repository.notificationSoundUri.first()
            Pair(mode, uri)
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to load sound settings, falling back to default", e)
            Pair("default", null)
        }
    }

    private suspend fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (mode, uriStr) = getSoundSettings()
            createChannelForMode(mode, uriStr)
        }
    }

    private fun createChannelForMode(mode: String, uriStr: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getActiveChannelId(mode, uriStr)
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Try to delete legacy default channel if it exists
            try {
                notificationManager.deleteNotificationChannel("arise_reminders_v4")
            } catch (e: Exception) {
                // Ignore
            }

            val name = "Arise Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = "Notifies you at the start of your routine tasks"
                
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                
                when (mode) {
                    "silent" -> {
                        setSound(null, null)
                    }
                    "custom" -> {
                        val soundUri = if (!uriStr.isNullOrEmpty()) {
                            Uri.parse(uriStr)
                        } else {
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        }
                        setSound(soundUri, audioAttributes)
                    }
                    else -> {
                        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        setSound(soundUri, audioAttributes)
                    }
                }
            }
            
            try {
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationHelper", "Notification channel '$channelId' created successfully for mode '$mode'")
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Exception while creating notification channel", e)
            }
        }
    }

    fun recreateNotificationChannel(mode: String, uriStr: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelForMode(mode, uriStr)
        }
    }

    suspend fun showNotification(title: String, content: String, notificationId: Int = java.util.UUID.randomUUID().hashCode()) {
        val (mode, uriStr) = getSoundSettings()
        val activeChannelId = getActiveChannelId(mode, uriStr)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannelForMode(mode, uriStr)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            val builder = NotificationCompat.Builder(context, activeChannelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            
            when (mode) {
                "silent" -> {
                    // No sound
                }
                "custom" -> {
                    val soundUri = if (!uriStr.isNullOrEmpty()) {
                        Uri.parse(uriStr)
                    } else {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    }
                    builder.setSound(soundUri)
                }
                else -> {
                    val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    builder.setSound(soundUri)
                }
            }
            
            notificationManager.notify(notificationId, builder.build())
            Log.d("NotificationHelper", "Notification shown successfully using channel '$activeChannelId'")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Exception while showing notification", e)
        }
    }

    fun scheduleTestReminder() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            add(Calendar.SECOND, 5)
        }
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("IS_TEST", true)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            88888, // Unique ID for test
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Permission to schedule test alarm denied", e)
        }
    }

    fun cancelAlarm(id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun scheduleAllAlarms(templates: List<RoutineTemplateEntity>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        
        for (template in templates) {
            val parts = template.startTimeStr.split(":")
            if (parts.size != 2) continue
            val hour = parts[0].toIntOrNull() ?: continue
            val minute = parts[1].toIntOrNull() ?: continue
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Subtract offset
            calendar.add(Calendar.MINUTE, -template.notificationOffsetMins)
            
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("TASK_TITLE", template.title)
                putExtra("TASK_TIME", template.startTimeStr)
                putExtra("OFFSET", template.notificationOffsetMins)
                putExtra("IS_REPORT", false)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                template.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                Log.e("NotificationHelper", "Permission to schedule exact alarm denied", e)
            }
        }
        
        // Schedule Report Notification at 8:30 PM (20:30)
        val reportCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (reportCalendar.before(now)) {
            reportCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val reportIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("IS_REPORT", true)
        }
        val reportPendingIntent = PendingIntent.getBroadcast(
            context,
            99999, // Unique ID
            reportIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Permission to schedule report alarm denied", e)
        }
        
        // Schedule a midnight Reschedule alarm to set up alarms for the next day
        val midnightCalendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val midnightIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("IS_MIDNIGHT_RESCHEDULE", true)
        }
        val midnightPendingIntent = PendingIntent.getBroadcast(
            context,
            99998, // Unique ID
            midnightIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnightCalendar.timeInMillis, midnightPendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnightCalendar.timeInMillis, midnightPendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnightCalendar.timeInMillis, midnightPendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "Permission to schedule midnight alarm denied", e)
        }
    }
}
