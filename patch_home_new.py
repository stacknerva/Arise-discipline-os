import re

with open("app/src/main/java/com/example/ui/HomeScreen.kt", "r") as f:
    content = f.read()

target1 = """            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = "CURRENT STREAK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currentStreak DAYS",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            val displayDate = SimpleDateFormat("EEEE\\nMMMM d, yyyy", Locale.getDefault()).format(currentCal.time)
            Text(
                text = displayDate.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val currentQuote = quote
            if (currentQuote != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {"""

replacement1 = """            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    text = "CURRENT STREAK",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$currentStreak DAYS",
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            val displayDate = SimpleDateFormat("EEEE\\nMMMM d, yyyy", Locale.getDefault()).format(currentCal.time)
            Text(
                text = displayDate.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val currentQuote = quote
            if (currentQuote != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {"""

target2 = """            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No quote available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Text(
                text = "ROUTINE",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val currentMinutes = currentCal.get(Calendar.HOUR_OF_DAY) * 60 + currentCal.get(Calendar.MINUTE)
            LazyColumn("""

replacement2 = """            } else {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "No quote available.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Text(
                text = "ROUTINE",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            LazyColumn("""

target3 = """                    val endCal = currentCal.clone() as java.util.Calendar
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

replacement3 = """                    val endCal = currentCal.clone() as java.util.Calendar
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

if target1 in content:
    content = content.replace(target1, replacement1)
else:
    print("Failed to replace target 1")

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("Failed to replace target 2")

if target3 in content:
    content = content.replace(target3, replacement3)
else:
    print("Failed to replace target 3")

with open("app/src/main/java/com/example/ui/HomeScreen.kt", "w") as f:
    f.write(content)

print("Patch script completed.")
