package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DailyTaskEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: DisciplineViewModel, onNavigateToReport: () -> Unit) {
    val tasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val quote by viewModel.quoteOfTheDay.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val currentCal by viewModel.currentTime.collectAsStateWithLifecycle()
    val isReportAvailable by viewModel.isReportAvailable.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "CURRENT STREAK\n$currentStreak DAYS",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            val displayDate = SimpleDateFormat("EEEE\nMMMM d, yyyy", Locale.getDefault()).format(Date())
            Text(
                text = displayDate.uppercase(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val currentQuote = quote
            if (currentQuote != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                        .padding(bottom = 32.dp)
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
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val currentMinutes = currentCal.get(Calendar.HOUR_OF_DAY) * 60 + currentCal.get(Calendar.MINUTE)

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(tasks) { index, task ->
                    val startMins = task.startTimeStr.split(":").let { (it[0].toInt() * 60) + it[1].toInt() }
                    val endMins = task.endTimeStr.split(":").let { (it[0].toInt() * 60) + it[1].toInt() }
                    
                    val isPast = currentMinutes >= endMins
                    val isCurrent = currentMinutes in startMins until endMins
                    
                    TaskTimelineItem(task = task, isPast = isPast, isCurrent = isCurrent, isLast = index == tasks.lastIndex)
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                }
            }
        }
        
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
            Text(
                text = "${task.startTimeStr} - ${task.endTimeStr}", 
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
