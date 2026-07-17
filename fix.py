import os
import re

path = 'app/src/main/java/com/example/data/NotificationHelper.kt'
with open(path, 'r') as f:
    content = f.read()

# Add import com.example.R
if 'import com.example.R' not in content:
    content = content.replace('import android.content.Context', 'import android.content.Context\nimport com.example.R')

# Replace the URI
content = re.sub(
    r'Uri\.parse\("\$\{ContentResolver\.SCHEME_ANDROID_RESOURCE\}://\$\{context\.packageName\}/raw/arise_notification"\)',
    'Uri.parse("${ContentResolver.SCHEME_ANDROID_RESOURCE}://${context.packageName}/${R.raw.arise_notification}")',
    content
)

with open(path, 'w') as f:
    f.write(content)
