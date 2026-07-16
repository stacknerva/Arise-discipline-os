import re

with open("app/src/main/java/com/example/data/NotificationHelper.kt", "r") as f:
    content = f.read()

target = """        // Schedule Report Notification at 8:30 PM (20:30)
        val reportCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        if (reportCalendar.after(now)) {
            val reportIntent = Intent(context, AlarmReceiver::class.java).apply {"""

replacement = """        // Schedule Report Notification at 8:30 PM (20:30)
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
            val reportIntent = Intent(context, AlarmReceiver::class.java).apply {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/data/NotificationHelper.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
