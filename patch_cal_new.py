import re

with open("app/src/main/java/com/example/ui/CalendarScreen.kt", "r") as f:
    content = f.read()

target1 = """                        val report = reportsMap[dateStr]
                        
                        val symbol = when {
                            report?.isSkipped == true -> "—"
                            report?.status == "perfect" -> "✓"
                            report?.status == "partial" -> "•"
                            report?.status == "missed" -> "✕"
                            else -> ""
                        }"""
                        
replacement1 = """                        val report = reportsMap[dateStr]
                        val isPast = dateStr < todayDateStr
                        val symbol = when {
                            report?.isSkipped == true -> "—"
                            report?.status == "perfect" -> "✓"
                            report?.status == "partial" -> "•"
                            report?.status == "missed" || (report == null && isPast) -> "✕"
                            else -> ""
                        }"""
                        
target2 = """                if (report != null) {
                    val statusStr = when {
                        report.isSkipped -> "Skipped"
                        report.status == "perfect" -> "Perfect"
                        report.status == "partial" -> "Partial"
                        report.status == "missed" -> "✕ No Report"
                        else -> "Pending"
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Status", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(statusStr, style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    if (report.isSubmitted) {
                        val completedCount = report.totalTasksCount - report.missedTasksCount
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Completed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$completedCount / ${report.totalTasksCount} Tasks", style = MaterialTheme.typography.bodyMedium)
                        }
                        
                        val missedTasks = selectedTasks.filter { it.isMissed }
                        if (missedTasks.isNotEmpty()) {
                            Text("Missed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                            missedTasks.forEach { mt ->
                                Text("• ${mt.title}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 2.dp))
                            }
                        }
                        
                        if (!report.reason.isNullOrBlank()) {
                            val reasonText = if (report.reason == "Other") report.otherReason ?: "Other" else report.reason
                            Text("Reason", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                            Text(reasonText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Text("No report for this date.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }"""

replacement2 = """                val isPast = selectedDate!! < todayDateStr
                val statusStr = when {
                    report?.isSkipped == true -> "Skipped"
                    report?.status == "perfect" -> "Perfect"
                    report?.status == "partial" -> "Partial"
                    report?.status == "missed" || (report == null && isPast) -> "✕ No Report"
                    else -> "Pending"
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Status", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(statusStr, style = MaterialTheme.typography.bodyMedium)
                }
                
                if (report?.isSubmitted == true) {
                    val completedCount = report.totalTasksCount - report.missedTasksCount
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Completed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedCount / ${report.totalTasksCount} Tasks", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    val missedTasks = selectedTasks.filter { it.isMissed }
                    if (missedTasks.isNotEmpty()) {
                        Text("Missed", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                        missedTasks.forEach { mt ->
                            Text("• ${mt.title}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 2.dp))
                        }
                    }
                    
                    if (!report.reason.isNullOrBlank()) {
                        val reasonText = if (report.reason == "Other") report.otherReason ?: "Other" else report.reason
                        Text("Reason", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                        Text(reasonText, style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (report == null && !isPast) {
                    // Future date, nothing extra to show
                }"""
                
if target1 in content:
    content = content.replace(target1, replacement1)
else:
    print("Failed to replace target 1")

if target2 in content:
    content = content.replace(target2, replacement2)
else:
    print("Failed to replace target 2")

with open("app/src/main/java/com/example/ui/CalendarScreen.kt", "w") as f:
    f.write(content)

print("Patch script completed.")
