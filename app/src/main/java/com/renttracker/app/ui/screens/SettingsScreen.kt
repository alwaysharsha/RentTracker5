package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val currency by viewModel.currency.collectAsState()
    val appLock by viewModel.appLock.collectAsState()
    val paymentMethods by viewModel.paymentMethods.collectAsState()
    
    var expandedCurrency by remember { mutableStateOf(false) }
    var showPaymentMethodsDialog by remember { mutableStateOf(false) }
    var showAddMethodDialog by remember { mutableStateOf(false) }
    var newMethodText by remember { mutableStateOf("") }

    val currencies = listOf("USD", "EUR", "GBP", "INR", "JPY", "CNY", "AUD", "CAD")

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
                    Text("Version: 1.0")
                    Text("Build: 1")
                    Text("Author: no28.iot@gmail.com")
                    Text("License: MIT")
                }
            }
        }
    }
    
    // Payment Methods Management Dialog
    if (showPaymentMethodsDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentMethodsDialog = false },
            title = { Text("Manage Payment Methods") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    paymentMethods.forEachIndexed { index, method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = method,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val updated = paymentMethods.toMutableList()
                                    updated.removeAt(index)
                                    viewModel.setPaymentMethods(updated)
                                }
                            ) {
                                Icon(Icons.Filled.Delete, "Delete")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPaymentMethodsDialog = false
                    showAddMethodDialog = true
                }) {
                    Text("Add New")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentMethodsDialog = false }) {
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
}
