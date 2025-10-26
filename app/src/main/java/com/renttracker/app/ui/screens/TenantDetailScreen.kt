package com.renttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.renttracker.app.data.model.Building
import com.renttracker.app.data.model.Tenant
import com.renttracker.app.ui.components.EditableDateField
import com.renttracker.app.ui.components.PhoneInputField
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.ValidationTextField
import com.renttracker.app.ui.viewmodel.BuildingViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import com.renttracker.app.ui.viewmodel.TenantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantDetailScreen(
    viewModel: TenantViewModel,
    buildingViewModel: BuildingViewModel,
    settingsViewModel: SettingsViewModel,
    tenantId: Long?,
    onNavigateBack: () -> Unit
) {
    val currency by settingsViewModel.currency.collectAsState()
    
    // Set default country code based on currency
    val defaultCountryCode = when (currency) {
        "INR" -> "91"
        "USD", "CAD" -> "1"
        "GBP" -> "44"
        "EUR" -> "33"  // France as default
        "JPY" -> "81"
        "CNY" -> "86"
        "AUD" -> "61"
        else -> "1"
    }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf(defaultCountryCode) }
    var mobile by remember { mutableStateOf("") }
    var countryCode2 by remember { mutableStateOf(defaultCountryCode) }
    var mobile2 by remember { mutableStateOf("") }
    var familyMembers by remember { mutableStateOf("") }
    var isCheckedOut by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    
    // New lease-related fields
    var selectedBuildingId by remember { mutableStateOf<Long?>(null) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var rentIncreaseDate by remember { mutableStateOf<Long?>(null) }
    var rent by remember { mutableStateOf("") }
    var securityDeposit by remember { mutableStateOf("") }
    var checkoutDate by remember { mutableStateOf<Long?>(null) }
    
    val buildings by buildingViewModel.buildings.collectAsState()
    var expandedBuilding by remember { mutableStateOf(false) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var existingTenant by remember { mutableStateOf<Tenant?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var nameError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }

    // Load existing tenant data for editing
    LaunchedEffect(tenantId) {
        if (tenantId != null) {
            viewModel.getTenantById(tenantId).collect { tenant ->
                tenant?.let {
                    existingTenant = it
                    name = it.name
                    email = it.email ?: ""
                    // Parse mobile number
                    val mobileStr = it.mobile
                    if (mobileStr.startsWith("+")) {
                        val parts = mobileStr.substring(1).split(Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"), 2)
                        if (parts.size == 2) {
                            countryCode = parts[0]
                            mobile = parts[1]
                        } else {
                            mobile = mobileStr.substring(1)
                        }
                    } else {
                        mobile = mobileStr
                    }
                    // Parse mobile2
                    it.mobile2?.let { mobile2Str ->
                        if (mobile2Str.startsWith("+")) {
                            val parts = mobile2Str.substring(1).split(Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)"), 2)
                            if (parts.size == 2) {
                                countryCode2 = parts[0]
                                mobile2 = parts[1]
                            } else {
                                mobile2 = mobile2Str.substring(1)
                            }
                        } else {
                            mobile2 = mobile2Str
                        }
                    }
                    familyMembers = it.familyMembers ?: ""
                    isCheckedOut = it.isCheckedOut
                    notes = it.notes ?: ""
                    selectedBuildingId = it.buildingId
                    startDate = it.startDate
                    rentIncreaseDate = it.rentIncreaseDate
                    rent = it.rent?.toString() ?: ""
                    securityDeposit = it.securityDeposit?.toString() ?: ""
                    checkoutDate = it.checkoutDate
                }
            }
        }
    }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = if (tenantId == null) "Add Tenant" else "Edit Tenant",
                onNavigationClick = onNavigateBack,
                actions = {
                    if (tenantId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        nameError = name.isBlank()
                        mobileError = mobile.isBlank()

                        if (!nameError && !mobileError) {
                            val tenant = Tenant(
                                id = tenantId ?: 0,
                                name = name,
                                email = email.ifBlank { null },
                                mobile = "+$countryCode$mobile",
                                mobile2 = if (mobile2.isNotEmpty()) "+$countryCode2$mobile2" else null,
                                familyMembers = familyMembers.ifBlank { null },
                                buildingId = selectedBuildingId,
                                startDate = startDate,
                                rentIncreaseDate = rentIncreaseDate,
                                rent = rent.toDoubleOrNull(),
                                securityDeposit = securityDeposit.toDoubleOrNull(),
                                checkoutDate = checkoutDate,
                                isCheckedOut = isCheckedOut,
                                notes = notes.ifBlank { null }
                            )
                            if (tenantId == null) {
                                viewModel.insertTenant(tenant) { onNavigateBack() }
                            } else {
                                viewModel.updateTenant(tenant) { onNavigateBack() }
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
        },
        snackbarHost = {
            if (errorMessage != null) {
                Snackbar {
                    Text(errorMessage!!)
                }
            }
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
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = "Tenant Name",
                isRequired = true,
                isError = nameError,
                errorMessage = "Name is required"
            )

            ValidationTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            PhoneInputField(
                countryCode = countryCode,
                onCountryCodeChange = { countryCode = it },
                phoneNumber = mobile,
                onPhoneNumberChange = {
                    mobile = it.filter { char -> char.isDigit() }
                    mobileError = false
                },
                label = "Mobile",
                isRequired = true
            )
            if (mobileError) {
                Text(
                    text = "Mobile is required",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PhoneInputField(
                countryCode = countryCode2,
                onCountryCodeChange = { countryCode2 = it },
                phoneNumber = mobile2,
                onPhoneNumberChange = { mobile2 = it.filter { char -> char.isDigit() } },
                label = "Mobile 2"
            )

            ValidationTextField(
                value = familyMembers,
                onValueChange = { familyMembers = it },
                label = "Family Members",
                singleLine = false,
                maxLines = 5
            )

            // Building Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedBuilding,
                onExpandedChange = { expandedBuilding = !expandedBuilding }
            ) {
                OutlinedTextField(
                    value = buildings.find { it.id == selectedBuildingId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Building") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBuilding) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
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

            // Start Date
            EditableDateField(
                value = startDate,
                onValueChange = { startDate = it },
                label = "Start Date"
            )

            // Rent Increase Date
            EditableDateField(
                value = rentIncreaseDate,
                onValueChange = { rentIncreaseDate = it },
                label = "Rent Increase Date"
            )

            // Rent
            OutlinedTextField(
                value = rent,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        rent = it
                    }
                },
                label = { Text("Rent") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Security Deposit
            OutlinedTextField(
                value = securityDeposit,
                onValueChange = {
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        securityDeposit = it
                    }
                },
                label = { Text("Security Deposit") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            // Checkout Date
            EditableDateField(
                value = checkoutDate,
                onValueChange = { checkoutDate = it },
                label = "Checkout Date"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Checked Out")
                Row {
                    Checkbox(
                        checked = isCheckedOut,
                        onCheckedChange = { checked ->
                            if (checked && tenantId != null) {
                                showCheckoutDialog = true
                            } else {
                                isCheckedOut = checked
                            }
                        }
                    )
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

        if (showDeleteDialog && existingTenant != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Tenant") },
                text = { Text("Are you sure you want to delete this tenant?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteTenant(existingTenant!!) { onNavigateBack() }
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

        if (showCheckoutDialog && existingTenant != null) {
            AlertDialog(
                onDismissRequest = { showCheckoutDialog = false },
                title = { Text("Checkout Tenant") },
                text = { Text("Are you sure you want to checkout this tenant?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.checkoutTenant(
                            existingTenant!!,
                            onSuccess = {
                                isCheckedOut = true
                                showCheckoutDialog = false
                            }
                        )
                    }) {
                        Text("Checkout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCheckoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
