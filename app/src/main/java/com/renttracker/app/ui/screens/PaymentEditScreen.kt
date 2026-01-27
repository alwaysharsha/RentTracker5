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
import com.renttracker.app.ui.components.Spinner
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

                // Payment Method Selection
                Spinner(
                    label = "Payment Method *",
                    items = paymentMethods,
                    selectedItem = selectedPaymentMethod,
                    onItemSelected = { selectedPaymentMethod = it },
                    modifier = Modifier.fillMaxWidth()
                )

                ValidationTextField(
                    value = transactionDetails,
                    onValueChange = { transactionDetails = it },
                    label = "Transaction Details"
                )

                // Payment Type Selection
                Column {
                    Spinner(
                        label = "Payment Type *",
                        items = PaymentStatus.values().toList(),
                        selectedItem = selectedPaymentType,
                        onItemSelected = { selectedPaymentType = it },
                        itemToString = { it.name },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (selectedPaymentType == PaymentStatus.FULL) {
                        Text(
                            text = "Select PARTIAL to track pending amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
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
            com.renttracker.app.ui.components.MonthYearPickerDialog(
                currentMonth = rentMonth!!,
                onDismiss = { showMonthPicker = false },
                onConfirm = { selectedMonth ->
                    rentMonth = selectedMonth
                    rentMonthError = false
                    showMonthPicker = false
                }
            )
        }
    }
}
