package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyReportEntity
import com.example.data.DailyTaskEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: DisciplineViewModel, onNavigateToReport: () -> Unit = {}) {
    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val reportsMap = remember(reports) { reports.associateBy { it.date } }
    
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 1000 })
    val coroutineScope = rememberCoroutineScope()
    
    var showMonthYearPicker by remember { mutableStateOf(false) }
    
    var selectedDate by remember { mutableStateOf<String?>(null) }
    
    val currentCal by viewModel.currentTime.collectAsStateWithLifecycle()
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentCal.time)
    
    val selectedTasksFlow = remember(selectedDate) {
        if (selectedDate != null) viewModel.getTasksForDateFlow(selectedDate!!) else flowOf(emptyList())
    }
    val selectedTasks by selectedTasksFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    
    val longestStreak = remember(reports) {
        var max = 0
        var current = 0
        val sortedReports = reports.sortedBy { it.date }
        for (r in sortedReports) {
            if (r.isSubmitted) {
                current++
                if (current > max) max = current
            } else if (r.isSkipped) {
                // Do nothing
            } else if (r.status == "missed") {
                current = 0
            }
        }
        max
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Text(
                text = "CALENDAR",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
        
        item {
            val page = pagerState.currentPage
            val monthOffset = page - initialPage
            
            val cal = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthOffset)
            }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            
            val firstDayOfMonth = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed, Sun=0
            
            val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time).uppercase()
            
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { 
                        coroutineScope.launch { pagerState.animateScrollToPage(page - 1) } 
                    }) {
                        Text("<", style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { showMonthYearPicker = true }
                    )
                    
                    TextButton(onClick = { 
                        coroutineScope.launch { pagerState.animateScrollToPage(page + 1) } 
                    }) {
                        Text(">", style = MaterialTheme.typography.titleMedium)
                    }
                }
                
                // Month Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 400.dp),
                    userScrollEnabled = false
                ) {
                    // Weekday Headers
                    val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
                    items(weekdays) { day ->
                        Text(
                            text = day,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    // Empty slots before first day
                    items(startDayOfWeek) {
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                    
                    // Days
                    items(daysInMonth) { dayIndex ->
                        val day = dayIndex + 1
                        val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
                        val report = reportsMap[dateStr]
                        
                        val symbol = when {
                            report?.isSkipped == true -> "—"
                            report?.status == "perfect" -> "✓"
                            report?.status == "partial" -> "•"
                            report?.status == "missed" -> "✕"
                            else -> ""
                        }
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    selectedDate = dateStr
                                }
                                .run {
                                    if (dateStr == selectedDate) {
                                        background(Color.Gray.copy(alpha = 0.2f), shape = CircleShape)
                                    } else if (dateStr == todayDateStr) {
                                        border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), shape = CircleShape)
                                    } else this
                                }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = day.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (report != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = symbol,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))
            
            if (selectedDate == null) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Select a date to view its report.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val report = reportsMap[selectedDate!!]
                val dateDisplay = try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val d = sdf.parse(selectedDate!!)
                    SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(d!!)
                } catch (e: Exception) {
                    selectedDate!!
                }
                
                Text(dateDisplay, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                
                if (report != null) {
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
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("MONTH SUMMARY", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            
            val page = pagerState.currentPage
            val monthOffset = page - initialPage
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            
            val currentMonthReports = reports.filter { it.date.startsWith(String.format("%04d-%02d", year, month + 1)) }
            val perfectDays = currentMonthReports.count { it.status == "perfect" }
            val partialDays = currentMonthReports.count { it.status == "partial" }
            val skippedDays = currentMonthReports.count { it.isSkipped }
            
            SummaryRow("Perfect Days", "$perfectDays")
            SummaryRow("Partial Days", "$partialDays")
            SummaryRow("Skipped Days", "$skippedDays")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SummaryRow("Current Streak", "$currentStreak Days")
            SummaryRow("Longest Streak", "$longestStreak Days")
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showMonthYearPicker) {
        var inputYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR).toString()) }
        var inputMonth by remember { mutableStateOf((Calendar.getInstance().get(Calendar.MONTH) + 1).toString()) }
        
        AlertDialog(
            onDismissRequest = { showMonthYearPicker = false },
            title = { Text("JUMP TO MONTH") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputYear,
                        onValueChange = { inputYear = it },
                        label = { Text("Year (YYYY)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputMonth,
                        onValueChange = { inputMonth = it },
                        label = { Text("Month (1-12)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val y = inputYear.toIntOrNull()
                    val m = inputMonth.toIntOrNull()
                    if (y != null && m != null && m in 1..12) {
                        val targetCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, y)
                            set(Calendar.MONTH, m - 1)
                        }
                        val currentCal = Calendar.getInstance()
                        val diffYear = targetCal.get(Calendar.YEAR) - currentCal.get(Calendar.YEAR)
                        val diffMonth = diffYear * 12 + targetCal.get(Calendar.MONTH) - currentCal.get(Calendar.MONTH)
                        coroutineScope.launch { pagerState.scrollToPage(initialPage + diffMonth) }
                        showMonthYearPicker = false
                    }
                }) { Text("GO") }
            },
            dismissButton = { TextButton(onClick = { showMonthYearPicker = false }) { Text("CANCEL") } }
        )
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
