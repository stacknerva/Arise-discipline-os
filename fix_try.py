import os
import re

path = 'app/src/main/java/com/example/data/NotificationHelper.kt'
with open(path, 'r') as f:
    content = f.read()

old_block = """            val channel = NotificationChannel(
                channelId,
                "Arise Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies you at the start of your routine tasks"
                
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationHelper", "Notification channel '$channelId' created successfully")
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Exception while creating notification channel", e)
            }"""

new_block = """            try {
                val channel = NotificationChannel(
                    channelId,
                    "Arise Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifies you at the start of your routine tasks"
                    
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                    setSound(soundUri, audioAttributes)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d("NotificationHelper", "Notification channel '$channelId' created successfully")
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Exception while creating notification channel", e)
            }"""

content = content.replace(old_block, new_block)
with open(path, 'w') as f:
    f.write(content)
