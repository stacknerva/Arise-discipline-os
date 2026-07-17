import re

path = 'app/src/main/java/com/example/data/NotificationHelper.kt'
with open(path, 'r') as f:
    content = f.read()

content = content.replace(
    'fun showNotification(title: String, content: String, notificationId: Int = System.currentTimeMillis().toInt())',
    'fun showNotification(title: String, content: String, notificationId: Int = java.util.UUID.randomUUID().hashCode())'
)

with open(path, 'w') as f:
    f.write(content)

