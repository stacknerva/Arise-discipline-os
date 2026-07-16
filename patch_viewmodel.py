import re

with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "r") as f:
    content = f.read()

target = """    fun manualSync(context: android.content.Context) {"""
replacement = """    @Suppress("DEPRECATION")
    fun manualSync(context: android.content.Context) {"""
content = content.replace(target, replacement)

with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "w") as f:
    f.write(content)
