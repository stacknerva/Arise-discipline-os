import re

with open("app/src/main/java/com/example/data/NotificationHelper.kt", "r") as f:
    content = f.read()

target = """            // Subtract offset
            calendar.add(Calendar.MINUTE, -template.notificationOffsetMins)
            
            if (calendar.before(now)) continue """

replacement = """            // Subtract offset
            calendar.add(Calendar.MINUTE, -template.notificationOffsetMins)
            
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/data/NotificationHelper.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
