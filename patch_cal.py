import re

with open("app/src/main/java/com/example/ui/CalendarScreen.kt", "r") as f:
    content = f.read()

target = """fun CalendarScreen(viewModel: DisciplineViewModel) {"""
replacement = """fun CalendarScreen(viewModel: DisciplineViewModel, onNavigateToReport: () -> Unit = {}) {"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/CalendarScreen.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
