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
    countryCode: String,
    onCountryCodeChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    label: String,
    isRequired: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = countryCode,
            onValueChange = { if (it.all { char -> char.isDigit() }) onCountryCodeChange(it) },
            label = { Text("Code") },
            modifier = Modifier.weight(0.3f),
            singleLine = true,
            prefix = { Text("+") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.all { char -> char.isDigit() }) onPhoneNumberChange(it) },
            label = {
                Text(
                    text = if (isRequired) "$label *" else label
                )
            },
            modifier = Modifier.weight(0.7f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
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

@Composable
fun EditableDateField(
    value: Long?,
    onValueChange: (Long?) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false
) {
    var textValue by remember(value) { 
        mutableStateOf(if (value != null) formatSimpleDate(value) else "") 
    }
    
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
                IconButton(onClick = { 
                    val newValue = System.currentTimeMillis()
                    onValueChange(newValue)
                    textValue = formatSimpleDate(newValue)
                }) {
                    Icon(Icons.Filled.CalendarToday, "Select Date")
                }
            }
        }
    )
}
