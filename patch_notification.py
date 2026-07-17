import re

path = 'app/src/main/java/com/example/data/NotificationHelper.kt'
with open(path, 'r') as f:
    content = f.read()

# Change the channel ID to v4
content = content.replace('val channelId = "arise_reminders_v3"', 'val channelId = "arise_reminders_v4"')

# Fix the URI to use the raw resource name instead of the integer ID
# There are two occurrences: one in createNotificationChannel, one in showNotification
old_uri = r'Uri\.parse\("\$\{ContentResolver\.SCHEME_ANDROID_RESOURCE\}://\$\{context\.packageName\}/\$\{R\.raw\.arise_notification\}"\)'
new_uri = r'Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/raw/arise_notification")'

content = re.sub(old_uri, new_uri, content)

with open(path, 'w') as f:
    f.write(content)

