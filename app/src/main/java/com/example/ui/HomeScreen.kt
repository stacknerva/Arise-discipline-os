package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyTaskEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.utils.formatTimeAmPm

@Composable
fun HomeScreen(viewModel: DisciplineViewModel, onNavigateToReport: () -> Unit) {
    val tasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val quote by viewModel.quoteOfTheDay.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val currentCal by viewModel.currentTime.collectAsStateWithLifecycle()
    val isReportAvailable by viewModel.isReportAvailable.collectAsStateWithLifecycle()
    val currentReport by viewModel.currentReport.collectAsStateWithLifecycle()
    val skippedMessageIndex by viewModel.skippedMessageIndex.collectAsStateWithLifecycle()
    
    val skippedMessages = listOf(
        "You wasted today.",
        "Today was lost.",
        "You made no progress today.",
        "Your goal is now one day further away.",
        "One day has been wasted.",
        "You delayed your future by one day.",
        "Today's progress: None.",
        "You chose comfort over discipline today.",
        "Nothing was achieved today.",
        "You failed to move forward today.",
        "This day will never come back.",
        "You lost one opportunity to improve.",
        "You postponed your growth today.",
        "You added another day to your journey.",
        "Today belongs to your excuses, not your goals.",
        "Your discipline ended for today.",
        "You gave up today's opportunity.",
        "You cannot recover this day.",
        "Time moved forward. You didn't.",
        "Your future received nothing from you today."
    )
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Column(
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
            
            val displayDate = SimpleDateFormat("EEEE\nMMMM d, yyyy", Locale.getDefault()).format(currentCal.time)
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
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "\"${currentQuote.text}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (currentQuote.author.isNotBlank() && currentQuote.author != "Unknown") {
                            Text(
                                text = "- ${currentQuote.author}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (currentQuote.source.isNotBlank()) {
                            Text(
                                text = currentQuote.source,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
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
            
            if (currentReport?.isSkipped == true) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DAY SKIPPED",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val messageIndex = if (skippedMessageIndex > 0) (skippedMessageIndex - 1) % skippedMessages.size else 0
                    val message = skippedMessages[messageIndex]
                    
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                Text(
                    text = "ROUTINE",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(tasks) { index, task ->
                        val startParts = task.startTimeStr.split(":")
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
                        val isPast = !currentCal.before(endCal)
                        
                        TaskTimelineItem(task = task, isPast = isPast, isCurrent = isCurrent, isLast = index == tasks.lastIndex)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                    }
                }
            }
        }
        
        if (currentReport?.isSkipped != true) {
            FloatingActionButton(
                onClick = {
                    if (isReportAvailable) {
                        onNavigateToReport()
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Daily Report is available only between 8:30 PM and 9:00 PM.")
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .alpha(if (isReportAvailable) 1f else 0.4f),
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = "Daily Report"
                )
            }
        }
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun TaskTimelineItem(task: DailyTaskEntity, isPast: Boolean, isCurrent: Boolean, isLast: Boolean) {
    val alpha = if (isPast) 0.4f else 1f
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .padding(vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            if (isCurrent) {
                Text("→", style = MaterialTheme.typography.bodyLarge)
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            if (!isLast) {
                Canvas(modifier = Modifier.width(2.dp).height(40.dp)) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2f
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.padding(top = 2.dp)) {
            val startAmPm = formatTimeAmPm(task.startTimeStr)
            val endAmPm = formatTimeAmPm(task.endTimeStr)
            Text(
                text = "$startAmPm - $endAmPm", 
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = task.title, 
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
