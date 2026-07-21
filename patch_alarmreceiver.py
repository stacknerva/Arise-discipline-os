import re

with open("app/src/main/java/com/example/data/AlarmReceiver.kt", "r") as f:
    content = f.read()

replacement = """
                if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.getBooleanExtra("IS_MIDNIGHT_RESCHEDULE", false)) {
                    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                        android.util.Log.d("AlarmReceiver", "BOOT_COMPLETED received")
                    } else {
                        android.util.Log.d("AlarmReceiver", "Midnight reschedule triggered")
                    }
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
                        android.util.Log.d("AlarmReceiver", "Skip Day suppression")
                        return@launch
                    }
                }
                
                val isReport = intent.getBooleanExtra("IS_REPORT", false)
                
                if (isTest) {
                    android.util.Log.d("AlarmReceiver", "Test reminder triggered")
                    helper.showNotification("Test Reminder", "Your notifications are working perfectly! You're all set.")
                    return@launch
                }
                
                if (isReport) {
                    android.util.Log.d("AlarmReceiver", "Report reminder triggered")
                    helper.showNotification("Daily Report", "Daily Report is now available.")
                    helper.rescheduleReportAlarm()
                } else {
                    android.util.Log.d("AlarmReceiver", "Task reminder triggered")
                    val title = intent.getStringExtra("TASK_TITLE") ?: "Task"
                    val offset = intent.getIntExtra("OFFSET", 5)
                    val templateId = intent.getIntExtra("TEMPLATE_ID", -1)
                    val startTimeStr = intent.getStringExtra("TASK_TIME")
                    
                    val msg = if (offset > 0) "Starts in $offset minutes." else "Starts now."
                    helper.showNotification(title, msg)
                    
                    if (templateId != -1 && startTimeStr != null) {
                        helper.rescheduleSpecificAlarm(templateId, title, startTimeStr, offset)
                    } else {
                        if (templateId == -1) android.util.Log.e("AlarmReceiver", "missing TEMPLATE_ID")
                        if (startTimeStr == null) android.util.Log.e("AlarmReceiver", "missing TASK_TIME")
                    }
                }
"""

start_str = '                if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.getBooleanExtra("IS_MIDNIGHT_RESCHEDULE", false)) {'
end_str = '                // Reschedule alarms for the next occurrence'

idx_start = content.find(start_str)
idx_end = content.find(end_str)

if idx_start != -1 and idx_end != -1:
    content = content[:idx_start] + replacement + content[idx_end:]

# Now remove the final scheduleAllAlarms block
final_block = """                // Reschedule alarms for the next occurrence
                if (repository != null) {
                    val templates = repository.getAllTemplatesSync()
                    helper.scheduleAllAlarms(templates)
                }"""

content = content.replace(final_block, "")

with open("app/src/main/java/com/example/data/AlarmReceiver.kt", "w") as f:
    f.write(content)
