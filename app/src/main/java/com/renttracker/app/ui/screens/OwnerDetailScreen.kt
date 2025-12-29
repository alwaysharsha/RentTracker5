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
import com.renttracker.app.data.model.Owner
import com.renttracker.app.ui.components.PhoneInputField
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.ValidationTextField
import com.renttracker.app.ui.viewmodel.OwnerViewModel
import com.renttracker.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailScreen(
    viewModel: OwnerViewModel,
    settingsViewModel: SettingsViewModel,
    ownerId: Long?,
    onNavigateBack: () -> Unit
) {
    val currency by settingsViewModel.currency.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
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
                    // Load mobile numbers as-is
                    mobile = it.mobile
                    mobile2 = it.mobile2 ?: ""
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
                        
                        if (!nameError && !mobileError) {
                            val owner = Owner(
                                id = ownerId ?: 0,
                                name = name,
                                email = email.ifBlank { null },
                                mobile = mobile,
                                mobile2 = if (mobile2.isNotEmpty()) mobile2 else null,
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
                phoneNumber = mobile,
                onPhoneNumberChange = {
                    mobile = it
                    mobileError = false
                },
                label = "Mobile",
                isRequired = true,
                isError = mobileError,
                errorMessage = "Mobile is required"
            )

            PhoneInputField(
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
