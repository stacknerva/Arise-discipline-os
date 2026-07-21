import re

with open("app/src/main/java/com/example/data/NotificationHelper.kt", "r") as f:
    content = f.read()

# Add TEMPLATE_ID to scheduleAllAlarms
content = content.replace(
    'putExtra("OFFSET", template.notificationOffsetMins)',
    'putExtra("OFFSET", template.notificationOffsetMins)\n                putExtra("TEMPLATE_ID", template.id)'
)

# Add specific reschedules
specific_methods = """
    fun rescheduleSpecificAlarm(templateId: Int, title: String, startTimeStr: String, notificationOffsetMins: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val parts = startTimeStr.split(":")
        if (parts.size != 2) {
            Log.e("NotificationHelper", "invalid template time")
            return
        }
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        calendar.add(Calendar.MINUTE, -notificationOffsetMins)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_TITLE", title)
            putExtra("TASK_TIME", startTimeStr)
            putExtra("OFFSET", notificationOffsetMins)
            putExtra("TEMPLATE_ID", templateId)
            putExtra("IS_REPORT", false)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            templateId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    Log.d("NotificationHelper", "permission fallback")
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            Log.d("NotificationHelper", "specific alarm rescheduled")
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "SecurityException", e)
        } catch (e: Exception) {
            Log.e("NotificationHelper", "general exceptions", e)
        }
    }

    fun rescheduleReportAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val reportCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        reportCalendar.add(Calendar.DAY_OF_YEAR, 1)
        
        val reportIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("IS_REPORT", true)
        }
        val reportPendingIntent = PendingIntent.getBroadcast(
            context,
            99999,
            reportIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)
                } else {
                    Log.d("NotificationHelper", "permission fallback")
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)
            }
            Log.d("NotificationHelper", "report alarm rescheduled")
        } catch (e: SecurityException) {
            Log.e("NotificationHelper", "SecurityException", e)
        } catch (e: Exception) {
            Log.e("NotificationHelper", "general exceptions", e)
        }
    }
"""

content = content.replace("fun scheduleAllAlarms(templates: List<RoutineTemplateEntity>) {", specific_methods + "\n    fun scheduleAllAlarms(templates: List<RoutineTemplateEntity>) {\n        Log.d(\"NotificationHelper\", \"scheduleAllAlarms started\")")
content = content.replace("Log.e(\"NotificationHelper\", \"Permission to schedule exact alarm denied\", e)", "Log.e(\"NotificationHelper\", \"SecurityException\", e)")
content = content.replace("Log.e(\"NotificationHelper\", \"Permission to schedule report alarm denied\", e)", "Log.e(\"NotificationHelper\", \"SecurityException\", e)")
content = content.replace("Log.e(\"NotificationHelper\", \"Permission to schedule midnight alarm denied\", e)", "Log.e(\"NotificationHelper\", \"SecurityException\", e)")
content = content.replace(
    'alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)\n                }',
    'alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)\n                    Log.d("NotificationHelper", "scheduled task alarm")\n                }'
)
content = content.replace(
    'alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)\n                    }',
    'alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)\n                        Log.d("NotificationHelper", "permission fallback")\n                    }'
)

content = content.replace(
    'alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)\n                }',
    'alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)\n                    Log.d("NotificationHelper", "scheduled report alarm")\n                }'
)
content = content.replace(
    'alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)\n                    }',
    'alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reportCalendar.timeInMillis, reportPendingIntent)\n                        Log.d("NotificationHelper", "permission fallback")\n                    }'
)

content = content.replace(
    'alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnightCalendar.timeInMillis, midnightPendingIntent)\n                }',
    'alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnightCalendar.timeInMillis, midnightPendingIntent)\n                    Log.d("NotificationHelper", "scheduled midnight alarm")\n                }'
)
content = content.replace(
    'alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnightCalendar.timeInMillis, midnightPendingIntent)\n                    }',
    'alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, midnightCalendar.timeInMillis, midnightPendingIntent)\n                        Log.d("NotificationHelper", "permission fallback")\n                    }'
)

content = content.replace("if (parts.size != 2) continue", "if (parts.size != 2) { Log.e(\"NotificationHelper\", \"invalid template time\"); continue }")

# Add completed log
content = content.replace('Log.e("NotificationHelper", "SecurityException", e)\n        }\n    }\n}', 'Log.e("NotificationHelper", "SecurityException", e)\n        }\n        Log.d("NotificationHelper", "scheduleAllAlarms completed")\n    }\n}')


with open("app/src/main/java/com/example/data/NotificationHelper.kt", "w") as f:
    f.write(content)
