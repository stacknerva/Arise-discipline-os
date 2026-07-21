package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.DisciplineApplication
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val helper = NotificationHelper(context)
        val app = context.applicationContext as? DisciplineApplication
        val repository = app?.repository
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.getBooleanExtra("IS_MIDNIGHT_RESCHEDULE", false)) {
                    if (repository != null) {
                        val templates = repository.getAllTemplatesSync()
                        helper.scheduleAllAlarms(templates)
                    }
                    return@launch
                }
                
                val isTest = intent.getBooleanExtra("IS_TEST", false)
                if (!isTest) {
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val dateStr = format.format(Date())
                    val report = if (repository != null) {
                        repository.getReportForDate(dateStr)
                    } else null
                    
                    if (report?.isSkipped == true) {
                        // Today is skipped, do not trigger any notifications/reminders
                        // But still reschedule alarms for tomorrow
                        if (repository != null) {
                            val templates = repository.getAllTemplatesSync()
                            helper.scheduleAllAlarms(templates)
                        }
                        return@launch
                    }
                }
                
                val isReport = intent.getBooleanExtra("IS_REPORT", false)
                
                if (isTest) {
                    helper.showNotification("Test Reminder", "Your notifications are working perfectly! You're all set.")
                    return@launch
                }
                
                if (isReport) {
                    helper.showNotification("Daily Report", "Daily Report is now available.")
                } else {
                    val title = intent.getStringExtra("TASK_TITLE") ?: "Task"
                    val offset = intent.getIntExtra("OFFSET", 5)
                    
                    val msg = if (offset > 0) "Starts in $offset minutes." else "Starts now."
                    helper.showNotification(title, msg)
                }
                
                // Reschedule alarms for the next occurrence
                if (repository != null) {
                    val templates = repository.getAllTemplatesSync()
                    helper.scheduleAllAlarms(templates)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
