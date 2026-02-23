package com.renttracker.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.renttracker.app.BuildConfig
import com.renttracker.app.MainActivity
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.Spinner
import com.renttracker.app.ui.viewmodel.ExportImportViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import com.renttracker.app.utils.Constants
import com.renttracker.app.utils.BackupTestUtils
import com.renttracker.app.utils.showErrorToast
import com.renttracker.app.utils.GoogleDriveBackupManager
import com.renttracker.app.utils.BackupScheduler
import com.renttracker.app.utils.GoogleSignInContract
import androidx.activity.compose.rememberLauncherForActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    exportImportViewModel: ExportImportViewModel,
    mainActivity: MainActivity,
    database: RentTrackerDatabase,
    preferencesManager: PreferencesManager
) {
    val currency by viewModel.currency.collectAsState()
    val appLock by viewModel.appLock.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    
    val exportStatus by exportImportViewModel.exportStatus.collectAsState()
    val importStatus by exportImportViewModel.importStatus.collectAsState()
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var showPaymentMethodsDialog by remember { mutableStateOf(false) }
    var showAddMethodDialog by remember { mutableStateOf(false) }
    var newMethodText by remember { mutableStateOf("") }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    // Google Drive backup state
    val driveBackupManager = remember { GoogleDriveBackupManager(context) }
    var isSignedIn by remember { mutableStateOf(driveBackupManager.isSignedIn()) }
    var lastBackupTime by remember { mutableStateOf<Long?>(null) }
    var backupInProgress by remember { mutableStateOf(false) }
    var restoreInProgress by remember { mutableStateOf(false) }
    var backupFrequency by remember { mutableStateOf(BackupScheduler.BackupFrequency.DISABLED) }
    var showBackupFrequencyDialog by remember { mutableStateOf(false) }
    
    // Setup Google Sign-In callback with MainActivity
    DisposableEffect(Unit) {
        android.util.Log.d("SettingsScreen", "Setting up Google Sign-In callback")
        mainActivity.onGoogleSignInResult = { account ->
            android.util.Log.d("SettingsScreen", "Sign-in callback invoked with account: ${account?.email}")
            if (account != null) {
                try {
                    android.util.Log.d("SettingsScreen", "Initializing Drive service for: ${account.email}")
                    driveBackupManager.initializeDriveService(account)
                    isSignedIn = true
                    android.util.Log.d("SettingsScreen", "Drive service initialized successfully")
                    Toast.makeText(context, "Signed in as ${account.email}", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.util.Log.e("SettingsScreen", "Failed to initialize Drive service", e)
                    Toast.makeText(context, "Failed to initialize Drive: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                android.util.Log.w("SettingsScreen", "Sign-in result was null - check MainActivity logs for details")
                Toast.makeText(
                    context, 
                    "Sign-in failed. Google Drive requires OAuth 2.0 setup in Google Cloud Console. See GOOGLE_DRIVE_SETUP.md for instructions.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        onDispose {
            android.util.Log.d("SettingsScreen", "Cleaning up Google Sign-In callback")
            mainActivity.onGoogleSignInResult = null
        }
    }
    
    // Load last backup time when signed in
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            val account = driveBackupManager.getSignedInAccount()
            if (account != null) {
                driveBackupManager.initializeDriveService(account)
                val result = driveBackupManager.getLastBackupTime()
                if (result.isSuccess) {
                    lastBackupTime = result.getOrNull()
                }
            }
        }
    }

    val currencies = listOf("USD", "EUR", "GBP", "INR", "JPY", "CNY", "AUD", "CAD")
    
    // Import launcher is now handled in MainActivity to avoid requestCode conflicts

    Scaffold(
        topBar = {
            RentTrackerTopBar(title = "Settings")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Currency Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Currency",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Spinner(
                        label = "Select Currency",
                        items = currencies,
                        selectedItem = currency,
                        onItemSelected = { viewModel.setCurrency(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Theme Mode Selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val themeMode by viewModel.themeMode.collectAsState()
                    val themeModes = listOf("System", "Light", "Dark")
                    val themeModeValues = listOf(
                        com.renttracker.app.data.preferences.PreferencesManager.THEME_MODE_SYSTEM,
                        com.renttracker.app.data.preferences.PreferencesManager.THEME_MODE_LIGHT,
                        com.renttracker.app.data.preferences.PreferencesManager.THEME_MODE_DARK
                    )
                    val selectedThemeDisplay = when (themeMode) {
                        com.renttracker.app.data.preferences.PreferencesManager.THEME_MODE_LIGHT -> "Light"
                        com.renttracker.app.data.preferences.PreferencesManager.THEME_MODE_DARK -> "Dark"
                        else -> "System"
                    }
                    Spinner(
                        label = "Select Theme",
                        items = themeModes,
                        selectedItem = selectedThemeDisplay,
                        onItemSelected = { selected ->
                            val index = themeModes.indexOf(selected)
                            if (index >= 0) {
                                viewModel.setThemeMode(themeModeValues[index])
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // App Lock
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "App Lock",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Require biometric authentication",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = appLock,
                        onCheckedChange = { viewModel.setAppLock(it) }
                    )
                }
            }

            // Payment Methods
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Payment Methods",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${paymentMethods.size} methods configured",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { showPaymentMethodsDialog = true }) {
                            Text("Manage")
                        }
                    }
                }
            }

            // Backup & Restore
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Export your data to a backup file or import from a previous backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportImportViewModel.exportData { uri ->
                                    exportedFileUri = uri
                                    showExportSuccess = uri != null
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = exportStatus !is ExportImportViewModel.ExportStatus.Exporting
                        ) {
                            if (exportStatus is ExportImportViewModel.ExportStatus.Exporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Filled.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Export")
                        }
                        
                        OutlinedButton(
                            onClick = { 
                                try {
                                    // Reset any existing import status
                                    exportImportViewModel.resetImportStatus()
                                    // Call MainActivity's import function to avoid requestCode conflicts
                                    mainActivity.launchImportFilePicker()
                                } catch (e: Exception) {
                                    showErrorToast(context, e.message)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = importStatus !is ExportImportViewModel.ImportStatus.Importing
                        ) {
                            if (importStatus is ExportImportViewModel.ImportStatus.Importing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Filled.Upload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Import")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Google Drive Cloud Backup Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Google Drive Cloud Backup",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Automatically backup your data to Google Drive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (!isSignedIn) {
                        Button(
                            onClick = {
                                try {
                                    val signInIntent = driveBackupManager.getSignInIntent()
                                    mainActivity.startActivityForResult(
                                        signInIntent,
                                        MainActivity.GOOGLE_SIGN_IN_REQUEST_CODE
                                    )
                                } catch (e: Exception) {
                                    android.util.Log.e("SettingsScreen", "Failed to launch sign-in", e)
                                    Toast.makeText(
                                        context,
                                        "Failed to start sign-in: ${e.message}. Please ensure Google Play Services is installed.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sign in to Google Drive")
                        }
                    } else {
                        // Show signed-in user info
                        val account = driveBackupManager.getSignedInAccount()
                        account?.let {
                            Text(
                                text = "Signed in as: ${it.email}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Last backup time
                        lastBackupTime?.let { time ->
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            Text(
                                text = "Last backup: ${dateFormat.format(Date(time))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        // Backup buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    backupInProgress = true
                                    coroutineScope.launch {
                                        try {
                                            val databasePath = context.getDatabasePath("rent_tracker.db").absolutePath
                                            val result = driveBackupManager.backupDatabase(databasePath)
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()
                                                val timeResult = driveBackupManager.getLastBackupTime()
                                                if (timeResult.isSuccess) {
                                                    lastBackupTime = timeResult.getOrNull()
                                                }
                                            } else {
                                                Toast.makeText(context, "Backup failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            backupInProgress = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !backupInProgress && !restoreInProgress
                            ) {
                                if (backupInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Filled.CloudUpload, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Backup Now")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    restoreInProgress = true
                                    coroutineScope.launch {
                                        try {
                                            val databasePath = context.getDatabasePath("rent_tracker.db").absolutePath
                                            val result = driveBackupManager.restoreDatabase(databasePath)
                                            if (result.isSuccess) {
                                                Toast.makeText(context, "Restore successful. Please restart the app.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Restore failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                        } finally {
                                            restoreInProgress = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !backupInProgress && !restoreInProgress
                            ) {
                                if (restoreInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Filled.CloudDownload, contentDescription = null)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restore")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Scheduled backup frequency
                        OutlinedButton(
                            onClick = { showBackupFrequencyDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Schedule: ${backupFrequency.name}")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Sign out button
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    driveBackupManager.signOut()
                                    isSignedIn = false
                                    lastBackupTime = null
                                    Toast.makeText(context, "Signed out from Google Drive", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign Out")
                        }
                    }
                }
            }
            
            // Export/Import Section continued
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Backup Test Button - Feature Flag Controlled
                    if (BuildConfig.ENABLE_TEST_BACKUP_SYSTEM) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        android.util.Log.d("SettingsScreen", "Starting backup test...")
                                        val testResult = BackupTestUtils.createAndValidateTestBackup(
                                            context,
                                            database,
                                            preferencesManager
                                        )
                                        
                                        if (testResult) {
                                            android.util.Log.d("SettingsScreen", "✅ Backup test PASSED")
                                            Toast.makeText(context, "Backup test PASSED! Check logs for details.", Toast.LENGTH_LONG).show()
                                        } else {
                                            android.util.Log.e("SettingsScreen", "❌ Backup test FAILED")
                                            Toast.makeText(context, "Backup test FAILED! Check logs for details.", Toast.LENGTH_LONG).show()
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("SettingsScreen", "Exception during backup test", e)
                                        Toast.makeText(context, "Backup test ERROR: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Filled.BugReport, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Backup System")
                        }
                    }
                }
            }

            // About Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version: 5.3.6")
                    Text("Build: 110")
                    Text("Author: no28.iot@gmail.com")
                    Text("License: MIT")
                }
            }
        }
    }
    
    // Payment Methods Management Dialog with Reordering
    if (showPaymentMethodsDialog) {
        var reorderableList by remember(paymentMethods) { mutableStateOf(paymentMethods.toMutableList()) }
        var draggedItemIndex by remember { mutableStateOf<Int?>(null) }
        var targetIndex by remember { mutableStateOf<Int?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                // Save reordered list
                if (reorderableList != paymentMethods) {
                    viewModel.setPaymentMethods(reorderableList)
                }
                showPaymentMethodsDialog = false
                draggedItemIndex = null
                targetIndex = null
            },
            title = { 
                Column {
                    Text("Manage Payment Methods")
                    Text(
                        "Long press and drag to reorder",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemsIndexed(
                        items = reorderableList,
                        key = { _, method -> method }
                    ) { index, method ->
                        var totalDragOffset by remember { mutableStateOf(0f) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    translationY = if (index == draggedItemIndex) totalDragOffset else 0f
                                    alpha = if (index == draggedItemIndex) 0.7f else 1f
                                    scaleX = if (index == draggedItemIndex) 1.05f else 1f
                                    scaleY = if (index == draggedItemIndex) 1.05f else 1f
                                }
                                .pointerInput(reorderableList.size) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedItemIndex = index
                                            totalDragOffset = 0f
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            totalDragOffset += dragAmount.y
                                            
                                            // Calculate approximate item height (card + spacing)
                                            val itemHeight = 64f
                                            val currentDraggedIndex = draggedItemIndex ?: return@detectDragGesturesAfterLongPress
                                            
                                            // Calculate new target index based on drag offset
                                            val offsetInItems = (totalDragOffset / itemHeight).toInt()
                                            val newTargetIndex = (currentDraggedIndex + offsetInItems)
                                                .coerceIn(0, reorderableList.size - 1)
                                            
                                            // Swap items if target changed
                                            if (newTargetIndex != currentDraggedIndex) {
                                                val mutableList = reorderableList.toMutableList()
                                                val item = mutableList.removeAt(currentDraggedIndex)
                                                mutableList.add(newTargetIndex, item)
                                                reorderableList = mutableList
                                                draggedItemIndex = newTargetIndex
                                                totalDragOffset = 0f
                                            }
                                        },
                                        onDragEnd = {
                                            draggedItemIndex = null
                                            totalDragOffset = 0f
                                            targetIndex = null
                                            // Save the reordered list
                                            viewModel.setPaymentMethods(reorderableList)
                                        },
                                        onDragCancel = {
                                            draggedItemIndex = null
                                            totalDragOffset = 0f
                                            targetIndex = null
                                        }
                                    )
                                }
                                .animateItemPlacement(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (index == targetIndex) 
                                    MaterialTheme.colorScheme.surfaceVariant 
                                else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (index == draggedItemIndex) 8.dp else 1.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.DragHandle,
                                    "Drag to reorder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                )
                                IconButton(
                                    onClick = {
                                        val mutableList = reorderableList.toMutableList()
                                        mutableList.removeAt(index)
                                        reorderableList = mutableList
                                        viewModel.setPaymentMethods(reorderableList)
                                    }
                                ) {
                                    Icon(Icons.Filled.Delete, "Delete")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Save any pending reorder
                    if (reorderableList != paymentMethods) {
                        viewModel.setPaymentMethods(reorderableList)
                    }
                    showPaymentMethodsDialog = false
                    showAddMethodDialog = true
                }) {
                    Text("Add New")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    // Save reordered list
                    if (reorderableList != paymentMethods) {
                        viewModel.setPaymentMethods(reorderableList)
                    }
                    showPaymentMethodsDialog = false 
                }) {
                    Text("Done")
                }
            }
        )
    }
    
    // Add Payment Method Dialog
    if (showAddMethodDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddMethodDialog = false
                newMethodText = ""
            },
            title = { Text("Add Payment Method") },
            text = {
                OutlinedTextField(
                    value = newMethodText,
                    onValueChange = { newMethodText = it },
                    label = { Text("Method Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newMethodText.isNotBlank()) {
                            val updated = paymentMethods.toMutableList()
                            updated.add(newMethodText.trim())
                            viewModel.setPaymentMethods(updated)
                            showAddMethodDialog = false
                            newMethodText = ""
                            showPaymentMethodsDialog = true
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddMethodDialog = false
                    newMethodText = ""
                    showPaymentMethodsDialog = true
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Export Success Dialog
    if (showExportSuccess) {
        AlertDialog(
            onDismissRequest = {
                showExportSuccess = false
                exportImportViewModel.resetExportStatus()
            },
            title = { Text("Export Successful") },
            text = {
                Column {
                    Text("Your data has been exported successfully.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "The backup file is saved in Downloads/RentTracker folder.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    exportedFileUri?.let { uri ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share backup file"))
                    }
                    showExportSuccess = false
                    exportImportViewModel.resetExportStatus()
                }) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportSuccess = false
                    exportImportViewModel.resetExportStatus()
                }) {
                    Text("Done")
                }
            }
        )
    }
    
    // Import Result Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
                exportImportViewModel.resetImportStatus()
            },
            title = {
                Text(
                    when (importStatus) {
                        is ExportImportViewModel.ImportStatus.Success -> "Import Successful"
                        is ExportImportViewModel.ImportStatus.Error -> "Import Failed"
                        else -> "Import"
                    }
                )
            },
            text = {
                Text(
                    when (importStatus) {
                        is ExportImportViewModel.ImportStatus.Success -> 
                            "Your data has been imported successfully."
                        is ExportImportViewModel.ImportStatus.Error -> 
                            (importStatus as ExportImportViewModel.ImportStatus.Error).message
                        else -> "Importing data..."
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    exportImportViewModel.resetImportStatus()
                }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Backup Frequency Dialog
    if (showBackupFrequencyDialog) {
        AlertDialog(
            onDismissRequest = { showBackupFrequencyDialog = false },
            title = { Text("Backup Schedule") },
            text = {
                Column {
                    Text("Choose how often to automatically backup to Google Drive:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    BackupScheduler.BackupFrequency.values().forEach { frequency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = backupFrequency == frequency,
                                onClick = {
                                    backupFrequency = frequency
                                    BackupScheduler.scheduleBackup(context, frequency)
                                    showBackupFrequencyDialog = false
                                    Toast.makeText(
                                        context,
                                        "Backup schedule updated to ${frequency.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (frequency) {
                                    BackupScheduler.BackupFrequency.DAILY -> "Daily"
                                    BackupScheduler.BackupFrequency.WEEKLY -> "Weekly"
                                    BackupScheduler.BackupFrequency.DISABLED -> "Disabled"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBackupFrequencyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
