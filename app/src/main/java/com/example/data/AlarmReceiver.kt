package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.DisciplineApplication
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        
        val isTest = intent.getBooleanExtra("IS_TEST", false)
        if (!isTest) {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = format.format(Date())
            val app = context.applicationContext as? DisciplineApplication
            val repository = app?.repository
            val report = if (repository != null) {
                runBlocking { repository.getReportForDate(dateStr) }
            } else null
            
            if (report?.isSkipped == true) {
                // Today is skipped, do not trigger any notifications/reminders
                return
            }
        }
        
        val isReport = intent.getBooleanExtra("IS_REPORT", false)
        val helper = NotificationHelper(context)
        
        if (isTest) {
            helper.showNotification("Test Reminder", "Your notifications are working perfectly! You're all set.")
            return
        }
        
        if (isReport) {
            helper.showNotification("Daily Report", "Daily Report is now available.")
            return
        }
        
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task"
        val offset = intent.getIntExtra("OFFSET", 5)
        
        val msg = if (offset > 0) "Starts in $offset minutes." else "Starts now."
        helper.showNotification(title, msg)
    }
}
