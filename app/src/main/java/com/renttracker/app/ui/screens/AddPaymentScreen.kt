package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
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
import com.renttracker.app.ui.viewmodel.TenantViewModel
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    viewModel: PaymentViewModel,
    tenantViewModel: TenantViewModel,
    settingsViewModel: SettingsViewModel,
    tenantId: Long,
    onNavigateBack: () -> Unit
) {
    val paymentMethods by settingsViewModel.paymentMethods.collectAsState()
    
    // Initialize rent month to current month (first day of month)
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    var rentMonth by remember { mutableStateOf(calendar.timeInMillis) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var amount by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember(paymentMethods) { 
        mutableStateOf(paymentMethods.firstOrNull() ?: "UPI") 
    }
    var transactionDetails by remember { mutableStateOf("") }
    var selectedPaymentType by remember { mutableStateOf(PaymentStatus.FULL) }
    var pendingAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tenant by remember { mutableStateOf<Tenant?>(null) }
    
    var amountError by remember { mutableStateOf(false) }
    var pendingAmountError by remember { mutableStateOf(false) }
    var rentMonthError by remember { mutableStateOf(false) }
    

    LaunchedEffect(tenantId) {
        tenantViewModel.getTenantById(tenantId).collect { t ->
            tenant = t
            if (t?.rent != null) {
                amount = t.rent.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = "Add Payment",
                onNavigationClick = onNavigateBack,
                actions = {
                    IconButton(onClick = {
                        amountError = amount.isBlank() || amount.toDoubleOrNull() == null
                        pendingAmountError = selectedPaymentType == PaymentStatus.PARTIAL && 
                            pendingAmount.isNotBlank() && pendingAmount.toDoubleOrNull() == null
                        rentMonthError = false // rentMonth always has value

                        if (!amountError && !pendingAmountError) {
                            val payment = Payment(
                                date = date,
                                amount = amount.toDouble(),
                                paymentMethod = selectedPaymentMethod,
                                transactionDetails = transactionDetails.ifBlank { null },
                                paymentType = selectedPaymentType,
                                pendingAmount = if (selectedPaymentType == PaymentStatus.PARTIAL && pendingAmount.isNotBlank()) 
                                    pendingAmount.toDoubleOrNull() else null,
                                notes = notes.ifBlank { null },
                                tenantId = tenantId,
                                rentMonth = rentMonth
                            )
                            viewModel.insertPayment(payment) { onNavigateBack() }
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
            // Rent Month field (at top as mandatory)
            OutlinedTextField(
                value = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(rentMonth)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Rent Month *") },
                trailingIcon = {
                    IconButton(onClick = { showMonthPicker = true }) {
                        Icon(Icons.Filled.CalendarToday, "Select Month")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = rentMonthError
            )
            
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
    
    // Month Picker Dialog
    if (showMonthPicker) {
        com.renttracker.app.ui.components.MonthYearPickerDialog(
            currentMonth = rentMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = { selectedMonth ->
                rentMonth = selectedMonth
                showMonthPicker = false
            }
        )
    }
}

