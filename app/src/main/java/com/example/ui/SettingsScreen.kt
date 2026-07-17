package com.example.ui

import android.Manifest
import android.content.ActivityNotFoundException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import com.example.data.NotificationHelper
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.RoutineTemplateEntity
import com.example.utils.formatTimeAmPm
import com.example.utils.parseTo24Hour
import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.example.R
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import androidx.credentials.CustomCredential
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import androidx.compose.runtime.rememberCoroutineScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.rotate


@Suppress("DEPRECATION")
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(viewModel: DisciplineViewModel) {
    val templates by viewModel.allTemplates.collectAsStateWithLifecycle()
    val isRoutineExpanded by viewModel.isRoutineExpanded.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val showCloudImportDialog by viewModel.showCloudImportDialog.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var showAddRoutine by remember { mutableStateOf<RoutineTemplateEntity?>(null) }
    var isAddingRoutine by remember { mutableStateOf(false) }
    var showSkipDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<RoutineTemplateEntity?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var signInError by remember { mutableStateOf<String?>(null) }
    var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }
    
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }
    
    val credentialManager = remember { CredentialManager.create(context) }
    
    val doGoogleSignIn = {
        coroutineScope.launch {
            try {
                val webClientId = context.getString(R.string.default_web_client_id)
                android.util.Log.d("SettingsScreen", "Starting Google Sign-In with Web Client ID: $webClientId")
                
                val hashedNonce = UUID.randomUUID().toString()
                android.util.Log.d("SettingsScreen", "Building GetGoogleIdOption...")
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setNonce(hashedNonce)
                    .setAutoSelectEnabled(false)
                    .build()
                    
                android.util.Log.d("SettingsScreen", "Building GetCredentialRequest...")
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                    
                android.util.Log.d("SettingsScreen", "Calling credentialManager.getCredential()...")
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                
                android.util.Log.d("SettingsScreen", "Received result, parsing credential...")
                val credential = result.credential
                if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    android.util.Log.d("SettingsScreen", "Creating GoogleIdTokenCredential from data...")
                    val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                    
                    android.util.Log.d("SettingsScreen", "Authenticating with Firebase...")
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            android.util.Log.d("SettingsScreen", "Firebase Auth successful.")
                            currentUser = FirebaseAuth.getInstance().currentUser
                            viewModel.handleSignIn(context)
                            signInError = null
                        } else {
                            val msg = "Firebase auth failed: ${authTask.exception?.message}"
                            android.util.Log.e("SettingsScreen", msg, authTask.exception)
                            signInError = msg
                        }
                    }
                } else {
                    android.util.Log.e("SettingsScreen", "Unexpected credential type: ${credential.type}")
                    signInError = "Unexpected credential type"
                }
            } catch (e: GetCredentialException) {
                val msg = "GetCredentialException: type=${e.type}, msg=${e.errorMessage}\n${e.message}"
                android.util.Log.e("SettingsScreen", msg, e)
                signInError = msg
            } catch (e: Exception) {
                val msg = "Exception during sign in: ${e.message}"
                android.util.Log.e("SettingsScreen", msg, e)
                signInError = msg
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri) { success, errorMsg ->
                if (success) {
                    Toast.makeText(context, "Backup imported successfully.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, errorMsg ?: "Invalid backup format", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context, uri) { success, errorMsg ->
                if (success) {
                    Toast.makeText(context, "Backup exported successfully to ${uri.lastPathSegment}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, errorMsg ?: "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        item {
            Text("SETTINGS", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 32.dp))
        }
        
        item {
            Text("ACCOUNT", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            AccountSection(
                account = currentUser,
                onSignIn = { doGoogleSignIn() }
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("SYNC STATUS", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            SyncStatusSection(
                account = currentUser,
                lastSyncTime = lastSyncTime,
                onSync = {
                    viewModel.manualSync(context)
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            RoutineSectionCard(
                isExpanded = isRoutineExpanded,
                onToggleExpand = { viewModel.setRoutineExpanded(!isRoutineExpanded) },
                templates = templates,
                onAddClick = { isAddingRoutine = true },
                onEditClick = { showAddRoutine = it },
                onDeleteClick = { routineToDelete = it }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            Text("MANAGEMENT", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
            
            ListItemAction("Skip Today", onClick = { showSkipDialog = true })
            ListItemAction("About", onClick = { showAboutDialog = true })
            
            ListItemAction("Import Routine", onClick = { showImportDialog = true })
            ListItemAction("Export Routine", onClick = {
                val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
                exportLauncher.launch("Arise_Backup_$dateStr.json")
            })
            
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    NotificationHelper(context).scheduleTestReminder()
                    Toast.makeText(context, "Test reminder scheduled. It will appear in about 5 seconds.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Notification permission is required for reminders.", Toast.LENGTH_LONG).show()
                }
            }

            ListItemAction("Test Reminder", onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                        NotificationHelper(context).scheduleTestReminder()
                        Toast.makeText(context, "Test reminder scheduled. It will appear in about 5 seconds.", Toast.LENGTH_SHORT).show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    NotificationHelper(context).scheduleTestReminder()
                    Toast.makeText(context, "Test reminder scheduled. It will appear in about 5 seconds.", Toast.LENGTH_SHORT).show()
                }
            })
            
            ListItemAction("Suggest a Feature", onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("gulshanattri229@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, "Arise Feature Suggestion")
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "No email app found on your device.", Toast.LENGTH_SHORT).show()
                }
            })
            
            Spacer(modifier = Modifier.height(48.dp))
            
            if (currentUser != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            googleSignInClient.signOut().addOnCompleteListener {
                                FirebaseAuth.getInstance().signOut()
                                currentUser = null
                                viewModel.handleSignOut()
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (signInError != null) {
        AlertDialog(
            onDismissRequest = { signInError = null },
            title = { Text("Sign In Error") },
            text = { Text(signInError ?: "") },
            confirmButton = {
                TextButton(onClick = { signInError = null }) { Text("OK") }
            }
        )
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

    if (showCloudImportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onImportCloudData(false) },
            title = { Text("Import Data") },
            text = { Text("Import your current local data into your Google account?") },
            confirmButton = {
                TextButton(onClick = { viewModel.onImportCloudData(true) }) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onImportCloudData(false) }) {
                    Text("Cancel")
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
    
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup") },
            text = { Text("Importing a backup will replace your current local data with the selected backup. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importLauncher.launch(arrayOf("application/json", "*/*"))
                }) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
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
    var start by remember { mutableStateOf(if (template != null) formatTimeAmPm(template.startTimeStr) else "") }
    var end by remember { mutableStateOf(if (template != null) formatTimeAmPm(template.endTimeStr) else "") }
    var offsetStr by remember { mutableStateOf(template?.notificationOffsetMins?.toString() ?: "5") }
    var orderStr by remember { mutableStateOf(template?.orderIndex?.toString() ?: "0") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (template == null) "ADD ROUTINE" else "EDIT ROUTINE") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                OutlinedTextField(value = start, onValueChange = { start = it }, label = { Text("Start Time (e.g. 2:00 PM)") })
                OutlinedTextField(value = end, onValueChange = { end = it }, label = { Text("End Time (e.g. 4:30 PM)") })
                OutlinedTextField(value = offsetStr, onValueChange = { offsetStr = it }, label = { Text("Notification Offset (mins)") })
                OutlinedTextField(value = orderStr, onValueChange = { orderStr = it }, label = { Text("Order Index") })
            }
        },
        confirmButton = { TextButton(onClick = { 
            val offset = offsetStr.toIntOrNull() ?: 5
            val order = orderStr.toIntOrNull() ?: 0
            onSave(title, parseTo24Hour(start), parseTo24Hour(end), offset, order) 
        }) { Text("SAVE") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}


@Suppress("DEPRECATION")
@Composable
fun AccountSection(account: com.google.firebase.auth.FirebaseUser?, onSignIn: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (account == null) {
                Text("Guest", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Sign in with Google to securely sync your Arise data across devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSignIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with Google")
                }
            } else {
                Text(account.displayName ?: "User", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    account.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun SyncStatusSection(account: com.google.firebase.auth.FirebaseUser?, lastSyncTime: String?, onSync: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (account != null) "Synced" else "Not Signed In",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (account != null) {
                        if (lastSyncTime != null) "Last Sync: $lastSyncTime" else "Never synced"
                    } else "Sign in with Google to enable cloud sync.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (account != null) {
                var isSyncing by remember { mutableStateOf(false) }
                val infiniteTransition = rememberInfiniteTransition(label = "sync")
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = if (isSyncing) 360f else 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "sync_angle"
                )

                IconButton(onClick = {
                    if (!isSyncing) {
                        isSyncing = true
                        onSync()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        modifier = Modifier.rotate(angle)
                    )
                }
                
                LaunchedEffect(lastSyncTime) {
                    if (isSyncing) isSyncing = false
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RoutineSectionCard(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    templates: List<RoutineTemplateEntity>,
    onAddClick: () -> Unit,
    onEditClick: (RoutineTemplateEntity) -> Unit,
    onDeleteClick: (RoutineTemplateEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Routine", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Manage your daily routine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle Routine",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onAddClick) { Text("ADD") }
                    }
                    
                    templates.sortedBy { it.orderIndex }.forEach { t ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onEditClick(t) },
                                    onLongClick = { onDeleteClick(t) }
                                )
                                .padding(vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val startAmPm = formatTimeAmPm(t.startTimeStr)
                                val endAmPm = formatTimeAmPm(t.endTimeStr)
                                Text("$startAmPm - $endAmPm", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(t.title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
