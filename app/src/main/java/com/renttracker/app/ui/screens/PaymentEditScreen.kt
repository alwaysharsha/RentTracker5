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
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentEditScreen(
    viewModel: PaymentViewModel,
    paymentId: Long,
    onNavigateBack: () -> Unit
) {
    var payment by remember { mutableStateOf<Payment?>(null) }
    var date by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var selectedBankType by remember { mutableStateOf<BankType?>(null) }
    var transactionDetails by remember { mutableStateOf("") }
    var selectedPaymentType by remember { mutableStateOf(PaymentStatus.FULL) }
    var pendingAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var amountError by remember { mutableStateOf(false) }
    var pendingAmountError by remember { mutableStateOf(false) }
    
    var expandedPaymentMethod by remember { mutableStateOf(false) }
    var expandedBankType by remember { mutableStateOf(false) }
    var expandedPaymentType by remember { mutableStateOf(false) }

    // Load existing payment data
    LaunchedEffect(paymentId) {
        val loadedPayment = viewModel.getPaymentById(paymentId)
        loadedPayment?.let {
            payment = it
            date = it.date
            amount = it.amount.toString()
            selectedPaymentMethod = it.paymentMethod
            selectedBankType = it.bankType
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

                        if (!amountError && !pendingAmountError && payment != null && date != null) {
                            val updatedPayment = payment!!.copy(
                                date = date!!,
                                amount = amount.toDouble(),
                                paymentMethod = selectedPaymentMethod,
                                bankType = selectedBankType,
                                transactionDetails = transactionDetails.ifBlank { null },
                                paymentType = selectedPaymentType,
                                pendingAmount = if (selectedPaymentType == PaymentStatus.PARTIAL && pendingAmount.isNotBlank()) 
                                    pendingAmount.toDoubleOrNull() else null,
                                notes = notes.ifBlank { null }
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
                        value = selectedPaymentMethod.name,
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
                        PaymentMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.name) },
                                onClick = {
                                    selectedPaymentMethod = method
                                    expandedPaymentMethod = false
                                }
                            )
                        }
                    }
                }

                // Bank Type Dropdown (only if Bank Transfer selected)
                if (selectedPaymentMethod == PaymentMethod.BANK_TRANSFER) {
                    ExposedDropdownMenuBox(
                        expanded = expandedBankType,
                        onExpandedChange = { expandedBankType = !expandedBankType }
                    ) {
                        OutlinedTextField(
                            value = selectedBankType?.name ?: "Select Bank Type",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bank Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBankType) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedBankType,
                            onDismissRequest = { expandedBankType = false }
                        ) {
                            BankType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedBankType = type
                                        expandedBankType = false
                                    }
                                )
                            }
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
    }
}
