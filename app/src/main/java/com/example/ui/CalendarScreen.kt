package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyReportEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: DisciplineViewModel, onNavigateToReport: () -> Unit) {
    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val reportsMap = remember(reports) { reports.associateBy { it.date } }
    
    val initialPage = 500
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 1000 })
    val coroutineScope = rememberCoroutineScope()
    
    var showMonthYearPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "CALENDAR",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
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
            
            Column(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier.fillMaxWidth()
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
                            else -> ""
                        }
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(40.dp)
                                .clickable(enabled = report != null) {
                                    if (report != null) {
                                        viewModel.setCurrentDate(dateStr)
                                        onNavigateToReport()
                                    }
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
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Very thin monthly streak bar
                val currentMonthReports = reports.filter { it.date.startsWith(String.format("%04d-%02d", year, month + 1)) }
                val perfectDays = currentMonthReports.count { it.status == "perfect" }
                val percentage = if (daysInMonth > 0) perfectDays.toFloat() / daysInMonth else 0f
                
                Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                    drawRect(color = Color.Gray.copy(alpha = 0.2f), size = size)
                    drawRect(color = Color.Gray, size = size.copy(width = size.width * percentage))
                }
            }
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
