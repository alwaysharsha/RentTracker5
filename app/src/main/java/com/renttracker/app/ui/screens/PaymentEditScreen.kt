package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentEditScreen(
    viewModel: PaymentViewModel,
    settingsViewModel: SettingsViewModel,
    paymentId: Long,
    onNavigateBack: () -> Unit
) {
    val paymentMethods by settingsViewModel.paymentMethods.collectAsState()
    
    var payment by remember { mutableStateOf<Payment?>(null) }
    var rentMonth by remember { mutableStateOf<Long?>(null) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("") }
    var transactionDetails by remember { mutableStateOf("") }
    var selectedPaymentType by remember { mutableStateOf(PaymentStatus.FULL) }
    var pendingAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var amountError by remember { mutableStateOf(false) }
    var pendingAmountError by remember { mutableStateOf(false) }
    var rentMonthError by remember { mutableStateOf(false) }
    
    var expandedPaymentMethod by remember { mutableStateOf(false) }
    var expandedPaymentType by remember { mutableStateOf(false) }

    // Load existing payment data
    LaunchedEffect(paymentId) {
        val loadedPayment = viewModel.getPaymentById(paymentId)
        loadedPayment?.let {
            payment = it
            rentMonth = it.rentMonth
            date = it.date
            amount = it.amount.toString()
            selectedPaymentMethod = it.paymentMethod
            transactionDetails = it.transactionDetails ?: ""
            selectedPaymentType = it.paymentType
            pendingAmount = it.pendingAmount?.toString() ?: ""
            notes = it.notes ?: ""
        }
    }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = "Edit Payment",
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        amountError = amount.isBlank() || amount.toDoubleOrNull() == null
                        pendingAmountError = selectedPaymentType == PaymentStatus.PARTIAL && 
                            pendingAmount.isNotBlank() && pendingAmount.toDoubleOrNull() == null

                        rentMonthError = rentMonth == null
                        
                        if (!amountError && !pendingAmountError && !rentMonthError && payment != null && date != null) {
                            val updatedPayment = payment!!.copy(
                                date = date!!,
                                amount = amount.toDouble(),
                                paymentMethod = selectedPaymentMethod,
                                transactionDetails = transactionDetails.ifBlank { null },
                                paymentType = selectedPaymentType,
                                pendingAmount = if (selectedPaymentType == PaymentStatus.PARTIAL && pendingAmount.isNotBlank()) 
                                    pendingAmount.toDoubleOrNull() else null,
                                notes = notes.ifBlank { null },
                                rentMonth = rentMonth!!
                            )
                            viewModel.updatePayment(updatedPayment) { onNavigateBack() }
                        }
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (payment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Rent Month field (at top as mandatory)
                rentMonth?.let { month ->
                    OutlinedTextField(
                        value = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(month)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rent Month *") },
                        trailingIcon = {
                            IconButton(onClick = { showMonthPicker = true }) {
                                Icon(Icons.Filled.CalendarToday, "Select Month")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = rentMonthError,
                        supportingText = if (rentMonthError) { { Text("Rent Month is required") } } else null
                    )
                }
                
                // Date field
                EditableDateField(
                    value = date,
                    onValueChange = { date = it ?: System.currentTimeMillis() },
                    label = "Payment Date",
                    isRequired = true
                )

                ValidationTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = false
                    },
                    label = "Amount",
                    isRequired = true,
                    isError = amountError,
                    errorMessage = "Valid amount is required"
                )

                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPaymentMethod,
                    onExpandedChange = { expandedPaymentMethod = !expandedPaymentMethod }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method *") },
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
                    value = transactionDetails,
                    onValueChange = { transactionDetails = it },
                    label = "Transaction Details"
                )

                // Payment Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedPaymentType,
                    onExpandedChange = { expandedPaymentType = !expandedPaymentType }
                ) {
                    OutlinedTextField(
                        value = selectedPaymentType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPaymentType) },
                        supportingText = if (selectedPaymentType == PaymentStatus.FULL) {
                            { Text("Select PARTIAL to track pending amount", style = MaterialTheme.typography.bodySmall) }
                        } else null,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedPaymentType,
                        onDismissRequest = { expandedPaymentType = false }
                    ) {
                        PaymentStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    selectedPaymentType = status
                                    expandedPaymentType = false
                                }
                            )
                        }
                    }
                }

                // Pending Amount Field (only for partial payments)
                if (selectedPaymentType == PaymentStatus.PARTIAL) {
                    ValidationTextField(
                        value = pendingAmount,
                        onValueChange = {
                            pendingAmount = it
                            pendingAmountError = false
                        },
                        label = "Pending Amount",
                        isError = pendingAmountError,
                        errorMessage = "Valid amount is required"
                    )
                }

                ValidationTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes",
                    singleLine = false,
                    maxLines = 5
                )
            }
        }

        if (showDeleteDialog && payment != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Payment") },
                text = { Text("Are you sure you want to delete this payment?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deletePayment(payment!!) { onNavigateBack() }
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
        
        // Month Picker Dialog
        if (showMonthPicker && rentMonth != null) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = rentMonth
            )
            DatePickerDialog(
                onDismissRequest = { showMonthPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            // Set to first day of selected month
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = selectedDate
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            rentMonth = cal.timeInMillis
                            rentMonthError = false
                        }
                        showMonthPicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMonthPicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
