package com.renttracker.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentTrackerTopBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions
    )
}

@Composable
fun ValidationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = "",
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = if (isRequired) "$label *" else label
            )
        },
        isError = isError,
        supportingText = {
            if (isError && errorMessage.isNotEmpty()) {
                Text(errorMessage)
            }
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = maxLines
    )
}

@Composable
fun PhoneInputField(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    OutlinedTextField(
        value = phoneNumber,
        onValueChange = onPhoneNumberChange,
        label = {
            Text(
                text = if (isRequired) "$label *" else label
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("+1234567890") },
        isError = isError,
        supportingText = {
            if (isError && errorMessage.isNotEmpty()) {
                Text(errorMessage)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatSimpleDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatCurrency(amount: Double, currency: String): String {
    val currencySymbol = when (currency) {
        "USD", "CAD", "AUD" -> "$"
        "GBP" -> "£"
        "EUR" -> "€"
        "INR" -> "₹"
        "JPY", "CNY" -> "¥"
        else -> "$"
    }
    val decimalFormat = java.text.DecimalFormat("#,##0.00")
    return "$currencySymbol${decimalFormat.format(amount)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableDateField(
    value: Long?,
    onValueChange: (Long?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var textValue by remember(value) { 
        mutableStateOf(if (value != null) formatSimpleDate(value) else "") 
    }
    var showDatePicker by remember { mutableStateOf(false) }
    
    OutlinedTextField(
        value = textValue,
        onValueChange = { newText ->
            textValue = newText
            // Try to parse the date
            if (newText.isEmpty()) {
                onValueChange(null)
            } else {
                try {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    sdf.isLenient = false
                    val date = sdf.parse(newText)
                    if (date != null) {
                        onValueChange(date.time)
                    }
                } catch (e: Exception) {
                    // Keep the text but don't update the value
                }
            }
        },
        label = {
            Text(
                text = if (isRequired) "$label *" else label
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("dd MMM yyyy") },
        isError = isError,
        supportingText = if (isError && errorMessage != null) { { Text(errorMessage) } } else null,
        trailingIcon = {
            Row {
                if (value != null) {
                    IconButton(onClick = { 
                        textValue = ""
                        onValueChange(null) 
                    }) {
                        Icon(Icons.Filled.Clear, "Clear Date")
                    }
                }
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Filled.CalendarToday, "Select Date")
                }
            }
        }
    )
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = value ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate ->
                        onValueChange(selectedDate)
                        textValue = formatSimpleDate(selectedDate)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
