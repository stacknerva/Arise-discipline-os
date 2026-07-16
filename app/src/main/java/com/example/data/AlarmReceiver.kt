package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        val isReport = intent.getBooleanExtra("IS_REPORT", false)
        val helper = NotificationHelper(context)
        
        if (isReport) {
            helper.showNotification("Daily Report", "Daily Report is now available.", 99999)
            return
        }
        
        val title = intent.getStringExtra("TASK_TITLE") ?: "Task"
        val offset = intent.getIntExtra("OFFSET", 5)
        
        val msg = if (offset > 0) "Starts in $offset minutes." else "Starts now."
        helper.showNotification(title, msg)
    }
}
