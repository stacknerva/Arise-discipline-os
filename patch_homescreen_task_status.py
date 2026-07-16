import re

with open("app/src/main/java/com/example/ui/HomeScreen.kt", "r") as f:
    content = f.read()

target = """                    val endCal = currentCal.clone() as java.util.Calendar
                    endCal.set(java.util.Calendar.HOUR_OF_DAY, endParts[0].toInt())
                    endCal.set(java.util.Calendar.MINUTE, endParts[1].toInt())
                    endCal.set(java.util.Calendar.SECOND, 0)
                    endCal.set(java.util.Calendar.MILLISECOND, 0)
                    
                    if (endCal.before(startCal) || endCal == startCal) {
                        // Overnight task
                        // Switch from Yesterday's execution to Today's execution 4 hours after the task ends
                        val cutoffCal = endCal.clone() as java.util.Calendar
                        cutoffCal.add(java.util.Calendar.HOUR_OF_DAY, 4)
                        
                        if (currentCal.before(cutoffCal)) {
                            startCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        } else {
                            endCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    
                    val isCurrent = !currentCal.before(startCal) && currentCal.before(endCal)
                    val isPast = !currentCal.before(endCal)"""

replacement = """                    val endCal = currentCal.clone() as java.util.Calendar
                    endCal.set(java.util.Calendar.HOUR_OF_DAY, endParts[0].toInt())
                    endCal.set(java.util.Calendar.MINUTE, endParts[1].toInt())
                    endCal.set(java.util.Calendar.SECOND, 0)
                    endCal.set(java.util.Calendar.MILLISECOND, 0)
                    
                    if (endCal.before(startCal) || endCal == startCal) {
                        // Overnight task spans midnight
                        val currentHour = currentCal.get(java.util.Calendar.HOUR_OF_DAY)
                        val currentMinute = currentCal.get(java.util.Calendar.MINUTE)
                        val nowMinutes = currentHour * 60 + currentMinute
                        val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()
                        
                        if (nowMinutes < endMinutes) {
                            // After midnight, but before the end time (e.g. 2:00 AM)
                            startCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        } else {
                            // Before start time (e.g. 3:00 PM), or after end time
                            endCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    
                    val isCurrent = !currentCal.before(startCal) && currentCal.before(endCal)
                    val isPast = !currentCal.before(endCal)"""

if target in content:
    content = content.replace(target, replacement)
    print("Patched successfully")
else:
    print("Target not found")

with open("app/src/main/java/com/example/ui/HomeScreen.kt", "w") as f:
    f.write(content)

