import re

path = 'app/src/main/java/com/example/data/AlarmReceiver.kt'
with open(path, 'r') as f:
    content = f.read()

# Replace 88888 and 99999 with nothing (so it uses the default argument)
content = content.replace(', 88888', '')
content = content.replace(', 99999', '')

with open(path, 'w') as f:
    f.write(content)

