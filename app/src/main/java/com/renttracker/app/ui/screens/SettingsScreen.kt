package com.renttracker.app.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.viewmodel.ExportImportViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    exportImportViewModel: ExportImportViewModel
) {
    val currency by viewModel.currency.collectAsState()
    val appLock by viewModel.appLock.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    
    val exportStatus by exportImportViewModel.exportStatus.collectAsState()
    val importStatus by exportImportViewModel.importStatus.collectAsState()
    
    val context = LocalContext.current
    
    var expandedCurrency by remember { mutableStateOf(false) }
    var showPaymentMethodsDialog by remember { mutableStateOf(false) }
    var showAddMethodDialog by remember { mutableStateOf(false) }
    var newMethodText by remember { mutableStateOf("") }
    var showExportSuccess by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportedFileUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val currencies = listOf("USD", "EUR", "GBP", "INR", "JPY", "CNY", "AUD", "CAD")
    
    // File picker for import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            exportImportViewModel.importData(it) { success ->
                showImportDialog = true
            }
        }
    }

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
                    ExposedDropdownMenuBox(
                        expanded = expandedCurrency,
                        onExpandedChange = { expandedCurrency = !expandedCurrency }
                    ) {
                        OutlinedTextField(
                            value = currency,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Currency") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCurrency) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCurrency,
                            onDismissRequest = { expandedCurrency = false }
                        ) {
                            currencies.forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr) },
                                    onClick = {
                                        viewModel.setCurrency(curr)
                                        expandedCurrency = false
                                    }
                                )
                            }
                        }
                    }
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
                            onClick = { importLauncher.launch("application/json") },
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
                    Text("Version: 2.0")
                    Text("Build: 2")
                    Text("Author: no28.iot@gmail.com")
                    Text("License: MIT")
                }
            }
        }
    }
    
    // Payment Methods Management Dialog with Reordering
    if (showPaymentMethodsDialog) {
        var reorderableList by remember(paymentMethods) { mutableStateOf(paymentMethods.toMutableList()) }
        var draggedItem by remember { mutableStateOf<String?>(null) }
        var draggedOverItem by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { 
                // Save reordered list
                if (reorderableList != paymentMethods) {
                    viewModel.setPaymentMethods(reorderableList)
                }
                showPaymentMethodsDialog = false 
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    reorderableList.forEachIndexed { index, method ->
                        var offsetY by remember { mutableStateOf(0f) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .graphicsLayer {
                                    translationY = if (method == draggedItem) offsetY else 0f
                                    alpha = if (method == draggedItem) 0.7f else 1f
                                }
                                .pointerInput(method) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            draggedItem = method
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            offsetY += dragAmount.y
                                            
                                            // Determine which item we're over
                                            val itemHeight = 60f // approximate
                                            val newIndex = ((index * itemHeight + offsetY) / itemHeight).toInt()
                                                .coerceIn(0, reorderableList.size - 1)
                                            
                                            if (newIndex != index && newIndex in reorderableList.indices) {
                                                val temp = reorderableList.toMutableList()
                                                temp.removeAt(index)
                                                temp.add(newIndex, method)
                                                reorderableList = temp
                                            }
                                        },
                                        onDragEnd = {
                                            draggedItem = null
                                            offsetY = 0f
                                        },
                                        onDragCancel = {
                                            draggedItem = null
                                            offsetY = 0f
                                        }
                                    )
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (method == draggedOverItem) 
                                    MaterialTheme.colorScheme.surfaceVariant 
                                else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                                )
                                IconButton(
                                    onClick = {
                                        reorderableList = reorderableList.toMutableList().apply {
                                            removeAt(index)
                                        }
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
                        "The backup file is saved in the exports folder.",
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
}
