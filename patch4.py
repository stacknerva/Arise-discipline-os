import re

with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "r") as f:
    content = f.read()

target = """    fun getQuoteOfTheDay() = quoteOfTheDay"""
replacement = """    fun getTasksForDateFlow(date: String) = repository.getTasksForDate(date)
    fun getQuoteOfTheDay() = quoteOfTheDay"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/DisciplineViewModel.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
