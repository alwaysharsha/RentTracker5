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
import com.renttracker.app.data.model.Vendor
import com.renttracker.app.data.model.VendorCategory
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.ValidationTextField
import com.renttracker.app.ui.viewmodel.VendorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDetailScreen(
    viewModel: VendorViewModel,
    vendorId: Long?,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(VendorCategory.OTHER) }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var existingVendor by remember { mutableStateOf<Vendor?>(null) }
    
    var nameError by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    LaunchedEffect(vendorId) {
        vendorId?.let {
            val vendor = viewModel.getVendorById(it)
            vendor?.let { v ->
                existingVendor = v
                name = v.name
                selectedCategory = v.category
                phone = v.phone ?: ""
                email = v.email ?: ""
                address = v.address ?: ""
                notes = v.notes ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = if (vendorId == null) "Add Vendor" else "Edit Vendor",
                onNavigationClick = onNavigateBack,
                actions = {
                    if (vendorId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        nameError = name.isBlank()
                        if (!nameError) {
                            val vendor = Vendor(
                                id = vendorId ?: 0,
                                name = name,
                                category = selectedCategory,
                                phone = phone.ifBlank { null },
                                email = email.ifBlank { null },
                                address = address.ifBlank { null },
                                notes = notes.ifBlank { null }
                            )
                            if (vendorId == null) {
                                viewModel.insertVendor(vendor) { onNavigateBack() }
                            } else {
                                viewModel.updateVendor(vendor) { onNavigateBack() }
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
                label = "Vendor Name",
                isRequired = true,
                isError = nameError,
                errorMessage = "Name is required"
            )

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = selectedCategory.name.replace("_", " "),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    VendorCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.replace("_", " ")) },
                            onClick = {
                                selectedCategory = category
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            ValidationTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Phone"
            )

            ValidationTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email"
            )

            ValidationTextField(
                value = address,
                onValueChange = { address = it },
                label = "Address",
                singleLine = false,
                maxLines = 3
            )

            ValidationTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notes",
                singleLine = false,
                maxLines = 5
            )
        }

        if (showDeleteDialog && existingVendor != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Vendor") },
                text = { Text("Are you sure you want to delete this vendor?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteVendor(existingVendor!!) { onNavigateBack() }
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
