import re

with open("app/src/main/java/com/example/ui/HomeScreen.kt", "r") as f:
    content = f.read()

target = """                    val startMins = task.startTimeStr.split(":").let { (it[0].toInt() * 60) + it[1].toInt() }
                    val endMins = task.endTimeStr.split(":").let { (it[0].toInt() * 60) + it[1].toInt() }
                    
                    val isOvernight = endMins < startMins
                    
                    val isPast = if (isOvernight) {
                        if (currentMinutes >= startMins) false
                        else if (currentMinutes < endMins) false
                        else {
                            val midpoint = endMins + (startMins - endMins) / 2
                            currentMinutes < midpoint
                        }
                    } else {
                        currentMinutes >= endMins
                    }
                    
                    val isCurrent = if (isOvernight) {
                        currentMinutes >= startMins || currentMinutes < endMins
                    } else {
                        currentMinutes in startMins until endMins
                    }"""

replacement = """                    val startParts = task.startTimeStr.split(":")
                    val endParts = task.endTimeStr.split(":")
                    
                    val startCal = currentCal.clone() as java.util.Calendar
                    startCal.set(java.util.Calendar.HOUR_OF_DAY, startParts[0].toInt())
                    startCal.set(java.util.Calendar.MINUTE, startParts[1].toInt())
                    startCal.set(java.util.Calendar.SECOND, 0)
                    startCal.set(java.util.Calendar.MILLISECOND, 0)
                    
                    val endCal = currentCal.clone() as java.util.Calendar
                    endCal.set(java.util.Calendar.HOUR_OF_DAY, endParts[0].toInt())
                    endCal.set(java.util.Calendar.MINUTE, endParts[1].toInt())
                    endCal.set(java.util.Calendar.SECOND, 0)
                    endCal.set(java.util.Calendar.MILLISECOND, 0)
                    
                    if (endCal.before(startCal)) {
                        // Overnight task
                        val currentHour = currentCal.get(java.util.Calendar.HOUR_OF_DAY)
                        if (currentHour < 12 && startParts[0].toInt() >= 12) {
                            // We are in the early morning of the NEXT day
                            startCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        } else {
                            // We are in the evening of the CURRENT day
                            endCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                    }
                    
                    val isCurrent = !currentCal.before(startCal) && currentCal.before(endCal)
                    val isPast = !currentCal.before(endCal)"""

if target in content:
    content = content.replace(target, replacement)
    with open("app/src/main/java/com/example/ui/HomeScreen.kt", "w") as f:
        f.write(content)
    print("Success")
else:
    print("Not found")
