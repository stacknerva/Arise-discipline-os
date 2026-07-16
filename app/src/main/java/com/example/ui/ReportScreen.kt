package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ReportScreen(viewModel: DisciplineViewModel, onBack: () -> Unit) {
    val isReportAvailable by viewModel.isReportAvailable.collectAsStateWithLifecycle()
    val report by viewModel.currentReport.collectAsStateWithLifecycle()
    val tasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()

    var selectedReason by remember { mutableStateOf("Overslept") }
    var otherReasonText by remember { mutableStateOf("") }
    var expandedReason by remember { mutableStateOf(false) }

    val reasons = listOf("Overslept", "Health", "Family Work", "Lack of Focus", "Schedule Change", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 32.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.padding(end = 16.dp)) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("DAILY REPORT", style = MaterialTheme.typography.titleLarge)
        }

        if (report?.isSubmitted == true && report?.isSkipped == false) {
            val total = report?.totalTasksCount ?: 1
            val completed = total - (report?.missedTasksCount ?: 0)
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SCHEDULE COMPLETED", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 24.dp))
                Text("Completed Tasks:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$completed / $total", style = MaterialTheme.typography.displayMedium, modifier = Modifier.padding(bottom = 32.dp))
                Text("Current Streak:", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$currentStreak Days", style = MaterialTheme.typography.displayMedium)
            }
        } else if (report?.isSkipped == true) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("SCHEDULE SKIPPED", style = MaterialTheme.typography.titleMedium)
            }
        } else if (!isReportAvailable) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("REPORT UNAVAILABLE", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Text("Available between 8:30 PM and 9:00 PM.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Report Form
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text("MISSED TASKS", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    Text("Select ONLY the tasks you failed to complete.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 16.dp))
                }

                items(tasks) { task ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTaskMissed(task) }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = task.isMissed,
                            onCheckedChange = { viewModel.toggleTaskMissed(task) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.onBackground,
                                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkmarkColor = MaterialTheme.colorScheme.background
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(task.title, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("REASON", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedReason = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedReason, color = MaterialTheme.colorScheme.onBackground)
                        }
                        DropdownMenu(
                            expanded = expandedReason,
                            onDismissRequest = { expandedReason = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            reasons.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r) },
                                    onClick = { selectedReason = r; expandedReason = false }
                                )
                            }
                        }
                    }
                    
                    if (selectedReason == "Other") {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = otherReasonText,
                            onValueChange = { otherReasonText = it },
                            label = { Text("Details") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Button(
                        onClick = {
                            val missedCount = tasks.count { it.isMissed }
                            viewModel.submitReport(missedCount, tasks.size, selectedReason, if (selectedReason == "Other") otherReasonText else null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground, contentColor = MaterialTheme.colorScheme.background)
                    ) {
                        Text("SUBMIT")
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
