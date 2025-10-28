package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.*
import com.renttracker.app.ui.components.EditableDateField
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.ValidationTextField
import com.renttracker.app.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    expenseViewModel: ExpenseViewModel,
    vendorViewModel: VendorViewModel,
    buildingViewModel: BuildingViewModel,
    settingsViewModel: SettingsViewModel,
    expenseId: Long?,
    onNavigateBack: () -> Unit
) {
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
    var expandedCategory by remember { mutableStateOf(false) }
    var expandedVendor by remember { mutableStateOf(false) }
    var expandedBuilding by remember { mutableStateOf(false) }
    var expandedPaymentMethod by remember { mutableStateOf(false) }

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

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = selectedCategory.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    ExpenseCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.replace("_", " ")) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedVendor,
                onExpandedChange = { expandedVendor = !expandedVendor }
            ) {
                OutlinedTextField(
                    value = vendors.find { it.id == selectedVendorId }?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Vendor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVendor) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedVendor,
                    onDismissRequest = { expandedVendor = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            selectedVendorId = null
                            expandedVendor = false
                        }
                    )
                    vendors.forEach { vendor ->
                        DropdownMenuItem(
                            text = { Text(vendor.name) },
                            onClick = {
                                selectedVendorId = vendor.id
                                expandedVendor = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedBuilding,
                onExpandedChange = { expandedBuilding = !expandedBuilding }
            ) {
                OutlinedTextField(
                    value = buildings.find { it.id == selectedBuildingId }?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Building") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuilding) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedBuilding,
                    onDismissRequest = { expandedBuilding = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            selectedBuildingId = null
                            expandedBuilding = false
                        }
                    )
                    buildings.forEach { building ->
                        DropdownMenuItem(
                            text = { Text(building.name) },
                            onClick = {
                                selectedBuildingId = building.id
                                expandedBuilding = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = expandedPaymentMethod,
                onExpandedChange = { expandedPaymentMethod = !expandedPaymentMethod }
            ) {
                OutlinedTextField(
                    value = selectedPaymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaymentMethod) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedPaymentMethod,
                    onDismissRequest = { expandedPaymentMethod = false }
                ) {
                    paymentMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                selectedPaymentMethod = method
                                expandedPaymentMethod = false
                            }
                        )
                    }
                }
            }

            ValidationTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                maxLines = 5
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
