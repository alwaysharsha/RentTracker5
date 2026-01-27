package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.*
import com.renttracker.app.ui.components.EditableDateField
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.ValidationTextField
import com.renttracker.app.ui.components.Spinner
import com.renttracker.app.ui.viewmodel.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.renttracker.app.MainActivity
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseViewModel: ExpenseViewModel,
    vendorViewModel: VendorViewModel,
    buildingViewModel: BuildingViewModel,
    settingsViewModel: SettingsViewModel,
    documentViewModel: DocumentViewModel,
    mainActivity: MainActivity,
    expenseId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val paymentMethods by settingsViewModel.paymentMethods.collectAsState()
    val vendors by vendorViewModel.vendors.collectAsState()
    val buildings by buildingViewModel.buildings.collectAsState()
    
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }
    var selectedVendorId by remember { mutableStateOf<Long?>(null) }
    var selectedBuildingId by remember { mutableStateOf<Long?>(null) }
    var selectedPaymentMethod by remember(paymentMethods) { 
        mutableStateOf(paymentMethods.firstOrNull() ?: "Cash") 
    }
    var notes by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var existingExpense by remember { mutableStateOf<Expense?>(null) }
    
    var descriptionError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    
    // Document upload states
    var showDocumentUploadDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var documentName by remember { mutableStateOf("") }
    var documentNotes by remember { mutableStateOf("") }
    val expenseDocuments = remember { mutableStateOf<List<Document>>(emptyList()) }

    LaunchedEffect(expenseId) {
        expenseId?.let {
            val expense = expenseViewModel.getExpenseById(it)
            expense?.let { e ->
                existingExpense = e
                description = e.description
                amount = e.amount.toString()
                date = e.date
                selectedCategory = e.category
                selectedVendorId = e.vendorId
                selectedBuildingId = e.buildingId
                selectedPaymentMethod = e.paymentMethod ?: paymentMethods.firstOrNull() ?: "Cash"
                notes = e.notes ?: ""
            }
        }
    }
    
    // Load expense documents
    LaunchedEffect(expenseId) {
        if (expenseId != null) {
            documentViewModel.getDocumentsByEntity(EntityType.EXPENSE, expenseId).collect { docs ->
                expenseDocuments.value = docs
            }
        }
    }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = if (expenseId == null) "Add Expense" else "Edit Expense",
                onNavigationClick = onNavigateBack,
                actions = {
                    if (expenseId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        descriptionError = description.isBlank()
                        amountError = amount.isBlank() || amount.toDoubleOrNull() == null

                        if (!descriptionError && !amountError) {
                            val expense = Expense(
                                id = expenseId ?: 0,
                                description = description,
                                amount = amount.toDouble(),
                                date = date,
                                category = selectedCategory,
                                vendorId = selectedVendorId,
                                buildingId = selectedBuildingId,
                                paymentMethod = selectedPaymentMethod,
                                notes = notes.ifBlank { null }
                            )
                            if (expenseId == null) {
                                expenseViewModel.insertExpense(expense) { onNavigateBack() }
                            } else {
                                expenseViewModel.updateExpense(expense) { onNavigateBack() }
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
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
            ValidationTextField(
                value = description,
                onValueChange = {
                    description = it
                    descriptionError = false
                },
                label = "Description",
                isRequired = true,
                isError = descriptionError,
                errorMessage = "Description is required"
            )

            ValidationTextField(
                value = amount,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        amount = it
                        amountError = false
                    }
                },
                label = "Amount",
                isRequired = true,
                isError = amountError,
                errorMessage = "Valid amount is required"
            )

            EditableDateField(
                value = date,
                onValueChange = { date = it ?: System.currentTimeMillis() },
                label = "Date",
                isRequired = true
            )

            Spinner(
                label = "Category *",
                items = ExpenseCategory.values().toList(),
                selectedItem = selectedCategory,
                onItemSelected = { selectedCategory = it },
                itemToString = { it.name.replace("_", " ") },
                modifier = Modifier.fillMaxWidth()
            )

            Spinner(
                label = "Vendor",
                items = listOf(null) + vendors,
                selectedItem = vendors.find { it.id == selectedVendorId },
                onItemSelected = { selectedVendorId = it?.id },
                itemToString = { it?.name ?: "None" },
                modifier = Modifier.fillMaxWidth()
            )

            Spinner(
                label = "Building",
                items = listOf(null) + buildings,
                selectedItem = buildings.find { it.id == selectedBuildingId },
                onItemSelected = { selectedBuildingId = it?.id },
                itemToString = { it?.name ?: "None" },
                modifier = Modifier.fillMaxWidth()
            )

            Spinner(
                label = "Payment Method",
                items = paymentMethods,
                selectedItem = selectedPaymentMethod,
                onItemSelected = { selectedPaymentMethod = it },
                modifier = Modifier.fillMaxWidth()
            )

            ValidationTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                maxLines = 5
            )
            
            // Documents Section (only for existing expenses)
            if (expenseId != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Documents (${expenseDocuments.value.size})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(
                                onClick = { showDocumentUploadDialog = true }
                            ) {
                                Icon(Icons.Filled.Upload, contentDescription = "Upload")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Upload")
                            }
                        }
                        
                        if (expenseDocuments.value.isNotEmpty()) {
                            Divider()
                            expenseDocuments.value.forEach { document ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = document.documentName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "${document.documentType.uppercase()} • ${documentViewModel.formatFileSize(document.fileSize)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            documentViewModel.deleteDocument(document) {
                                                // Document deleted
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No documents uploaded",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Document Upload Dialog
        if (showDocumentUploadDialog && expenseId != null) {
            AlertDialog(
                onDismissRequest = {
                    showDocumentUploadDialog = false
                    documentName = ""
                    documentNotes = ""
                },
                title = { Text("Upload Document") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = documentName,
                            onValueChange = { documentName = it },
                            label = { Text("Document Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = documentNotes,
                            onValueChange = { documentNotes = it },
                            label = { Text("Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Choose upload method:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    try {
                                        mainActivity.launchDocumentFilePicker(
                                            documentName.takeIf { it.isNotBlank() },
                                            EntityType.EXPENSE,
                                            documentNotes.takeIf { it.isNotBlank() },
                                            expenseId
                                        )
                                        showDocumentUploadDialog = false
                                        documentName = ""
                                        documentNotes = ""
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("File")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    try {
                                        when {
                                            ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) == PackageManager.PERMISSION_GRANTED -> {
                                                mainActivity.launchDocumentCamera(
                                                    documentName.takeIf { it.isNotBlank() },
                                                    EntityType.EXPENSE,
                                                    documentNotes.takeIf { it.isNotBlank() },
                                                    expenseId
                                                )
                                                showDocumentUploadDialog = false
                                                documentName = ""
                                                documentNotes = ""
                                            }
                                            else -> {
                                                showDocumentUploadDialog = false
                                                showPermissionDialog = true
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera")
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDocumentUploadDialog = false
                            documentName = ""
                            documentNotes = ""
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Camera Permission Dialog
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Camera Permission Required") },
                text = { Text("Camera permission is required to take photos. Please grant permission in app settings.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPermissionDialog = false
                            val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:${context.packageName}")
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Open Settings")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteDialog && existingExpense != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Expense") },
                text = { Text("Are you sure you want to delete this expense?") },
                confirmButton = {
                    TextButton(onClick = {
                        expenseViewModel.deleteExpense(existingExpense!!) { onNavigateBack() }
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
