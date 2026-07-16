import re

with open("app/src/main/java/com/example/DisciplineApplication.kt", "r") as f:
    content = f.read()

target = """                initialRoutines.forEach { repository.insertTemplate(it) }
            }
        }
    }
}"""
replacement = """                initialRoutines.forEach { repository.insertTemplate(it) }
            } else {
                // Fix for existing Sleep routines with wrong end time
                val sleepRoutine = templates.find { it.title == "Sleep" && it.startTimeStr == "21:00" && it.endTimeStr == "21:00" }
                if (sleepRoutine != null) {
                    repository.updateTemplate(sleepRoutine.copy(endTimeStr = "04:00"))
                }
            }
        }
    }
}"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/DisciplineApplication.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
