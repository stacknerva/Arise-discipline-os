import re

with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "r") as f:
    content = f.read()

target = """    fun setCurrentDate(date: String) {"""
replacement = """    fun getTasksForDateFlow(date: String) = repository.getTasksForDate(date)

    fun setCurrentDate(date: String) {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
