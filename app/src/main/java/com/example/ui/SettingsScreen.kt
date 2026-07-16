package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RoutineTemplateEntity

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(viewModel: DisciplineViewModel) {
    val templates by viewModel.allTemplates.collectAsStateWithLifecycle()

    var showAddRoutine by remember { mutableStateOf<RoutineTemplateEntity?>(null) }
    var isAddingRoutine by remember { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<RoutineTemplateEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Text("SETTINGS", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 32.dp))
        }

        item {
            SectionHeader("ROUTINE", onAddClick = { isAddingRoutine = true })
        }
        items(templates.sortedBy { it.orderIndex }) { t ->
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { showAddRoutine = t },
                        onLongClick = { routineToDelete = t }
                    )
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("${t.startTimeStr} - ${t.endTimeStr}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(t.title, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(48.dp))
            Text("MANAGEMENT", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            
            ListItemAction("Skip Today", onClick = { showSkipDialog = true })
            ListItemAction("About", onClick = { showAboutDialog = true })
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (isAddingRoutine) {
        AddRoutineDialog(
            template = null,
            onDismiss = { isAddingRoutine = false },
            onSave = { title, start, end, offset, order ->
                viewModel.addRoutineTemplate(RoutineTemplateEntity(title = title, startTimeStr = start, endTimeStr = end, notificationOffsetMins = offset, orderIndex = order))
                isAddingRoutine = false
            }
        )
    }

    showAddRoutine?.let { template ->
        AddRoutineDialog(
            template = template,
            onDismiss = { showAddRoutine = null },
            onSave = { title, start, end, offset, order ->
                viewModel.updateRoutineTemplate(template.copy(title = title, startTimeStr = start, endTimeStr = end, notificationOffsetMins = offset, orderIndex = order))
                showAddRoutine = null
            }
        )
    }

    routineToDelete?.let { t ->
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            title = { Text("DELETE ROUTINE") },
            text = { Text("Are you sure you want to delete '${t.title}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRoutineTemplate(t.id)
                    routineToDelete = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text("CANCEL")
                }
            }
        )
    }

    if (showSkipDialog) {
        AlertDialog(
            onDismissRequest = { showSkipDialog = false },
            title = { Text("Skip Today's Schedule?") },
            text = { Text("Use this only if your schedule genuinely could not be followed because of illness, travel, exams, emergencies or unavoidable circumstances.\n\nThis will not increase or break your streak.") },
            confirmButton = { TextButton(onClick = { viewModel.skipToday(); showSkipDialog = false }) { Text("SKIP TODAY", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showSkipDialog = false }) { Text("CANCEL") } }
        )
    }
    
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("ABOUT") },
            text = { Text("Minimal daily record keeper.\n\nOffline-first, no excuses.") },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("OK") } }
        )
    }
}

@Composable
private fun ListItemAction(text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text.replace(" >", ""), style = MaterialTheme.typography.bodyLarge)
        Text(">", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionHeader(title: String, onAddClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onAddClick) { Text("ADD") }
    }
}

@Composable
fun AddRoutineDialog(template: RoutineTemplateEntity?, onDismiss: () -> Unit, onSave: (String, String, String, Int, Int) -> Unit) {
    var title by remember { mutableStateOf(template?.title ?: "") }
    var start by remember { mutableStateOf(template?.startTimeStr ?: "") }
    var end by remember { mutableStateOf(template?.endTimeStr ?: "") }
    var offsetStr by remember { mutableStateOf(template?.notificationOffsetMins?.toString() ?: "5") }
    var orderStr by remember { mutableStateOf(template?.orderIndex?.toString() ?: "0") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "ADD ROUTINE" else "EDIT ROUTINE") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Time (HH:mm)") })
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Time (HH:mm)") })
                OutlinedTextField(value = offsetStr, onValueChange = { offsetStr = it }, label = { Text("Notification Offset (mins)") })
                OutlinedTextField(value = orderStr, onValueChange = { orderStr = it }, label = { Text("Order Index") })
            }
        },
        confirmButton = { TextButton(onClick = { 
            val offset = offsetStr.toIntOrNull() ?: 5
            val order = orderStr.toIntOrNull() ?: 0
            onSave(title, start, end, offset, order) 
        }) { Text("SAVE") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}
