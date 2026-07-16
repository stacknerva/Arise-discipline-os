import re

with open("app/src/main/java/com/example/DisciplineApplication.kt", "r") as f:
    content = f.read()

target = """RoutineTemplateEntity(title = "Sleep", startTimeStr = "21:00", endTimeStr = "21:00", orderIndex = 7)"""
replacement = """RoutineTemplateEntity(title = "Sleep", startTimeStr = "21:00", endTimeStr = "04:00", orderIndex = 7)"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/DisciplineApplication.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
