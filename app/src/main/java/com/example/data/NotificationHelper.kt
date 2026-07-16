package com.example.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Calendar

class NotificationHelper(private val context: Context) {
    private val channelId = "arise_routine_channel"
    
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Routine Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you at the start of your routine tasks"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, content: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        
        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleAlarmsForTasks(tasks: List<DailyTaskEntity>, templates: List<RoutineTemplateEntity>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = Calendar.getInstance()
        
        for (task in tasks) {
            val template = templates.find { it.id == task.templateId } ?: continue
            val parts = task.startTimeStr.split(":")
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
                putExtra("TASK_TITLE", task.title)
                putExtra("TASK_TIME", task.startTimeStr)
                putExtra("OFFSET", template.notificationOffsetMins)
                putExtra("IS_REPORT", false)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                task.id,
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
        
        if (true) {
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
        }
    }
}
