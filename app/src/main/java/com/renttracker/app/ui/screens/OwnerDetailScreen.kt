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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.renttracker.app.RentTrackerApplication
import com.renttracker.app.data.model.EntityType
import com.renttracker.app.data.model.Owner
import com.renttracker.app.ui.components.DocumentUploadComponent
import com.renttracker.app.ui.components.PhoneInputField
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.ValidationTextField
import com.renttracker.app.ui.viewmodel.DocumentViewModel
import com.renttracker.app.ui.viewmodel.OwnerViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailScreen(
    viewModel: OwnerViewModel,
    settingsViewModel: SettingsViewModel,
    documentViewModel: DocumentViewModel,
    ownerId: Long?,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as RentTrackerApplication
    val currency by settingsViewModel.currency.collectAsState()
    
    // Get default country code based on currency
    val defaultCountryCode = when (currency) {
        "USD", "CAD" -> "1"  // US/Canada
        "GBP" -> "44"  // UK
        "EUR" -> "33"  // France (default for EUR)
        "INR" -> "91"  // India
        "JPY" -> "81"  // Japan
        "CNY" -> "86"  // China
        "AUD" -> "61"  // Australia
        else -> "1"  // Default to US
    }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf(defaultCountryCode) }
    var mobile by remember { mutableStateOf("") }
    var countryCode2 by remember { mutableStateOf(defaultCountryCode) }
    var mobile2 by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var existingOwner by remember { mutableStateOf<Owner?>(null) }
    
    var nameError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    var mobileFormatError by remember { mutableStateOf(false) }

    // Load existing owner data for editing
    LaunchedEffect(ownerId) {
        if (ownerId != null) {
            viewModel.getOwnerById(ownerId).collect { owner ->
                owner?.let {
                    existingOwner = it
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
                    address = it.address ?: ""
                }
            }
        }
    }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = if (ownerId == null) "Add Owner" else "Edit Owner",
                onNavigationClick = onNavigateBack,
                actions = {
                    if (ownerId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        nameError = name.isBlank()
                        mobileError = mobile.isBlank()
                        
                        // Validate mobile number format (digits only, 10-15 digits)
                        mobileFormatError = if (mobile.isNotBlank()) {
                            !mobile.matches(Regex("^[0-9]{7,15}$"))
                        } else false

                        if (!nameError && !mobileError && !mobileFormatError) {
                            val owner = Owner(
                                id = ownerId ?: 0,
                                name = name,
                                email = email.ifBlank { null },
                                mobile = "+$countryCode$mobile",
                                mobile2 = if (mobile2.isNotEmpty()) "+$countryCode2$mobile2" else null,
                                address = address.ifBlank { null }
                            )
                            if (ownerId == null) {
                                viewModel.insertOwner(owner) { onNavigateBack() }
                            } else {
                                viewModel.updateOwner(owner) { onNavigateBack() }
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
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = "Owner Name",
                isRequired = true,
                isError = nameError,
                errorMessage = "Name is required"
            )

            ValidationTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                isRequired = false
            )

            PhoneInputField(
                countryCode = countryCode,
                onCountryCodeChange = { countryCode = it },
                phoneNumber = mobile,
                onPhoneNumberChange = {
                    mobile = it.filter { char -> char.isDigit() }
                    mobileError = false
                    mobileFormatError = false
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
            if (mobileFormatError) {
                Text(
                    text = "Invalid mobile number format (7-15 digits)",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            PhoneInputField(
                countryCode = countryCode2,
                onCountryCodeChange = { countryCode2 = it },
                phoneNumber = mobile2,
                onPhoneNumberChange = { mobile2 = it },
                label = "Mobile 2"
            )

            ValidationTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address",
                singleLine = false,
                maxLines = 3
            )
            
            // Add document upload component (only show for existing owners)
            if (ownerId != null && ownerId > 0) {
                Divider()
                DocumentUploadComponent(
                    documentViewModel = documentViewModel,
                    entityType = EntityType.OWNER,
                    entityId = ownerId,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showDeleteDialog && existingOwner != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Owner") },
                text = { Text("Are you sure you want to delete this owner?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteOwner(existingOwner!!) { onNavigateBack() }
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
