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
import com.renttracker.app.data.model.Building
import com.renttracker.app.data.model.Owner
import com.renttracker.app.data.model.PropertyType
import com.renttracker.app.ui.components.RentTrackerTopBar
import com.renttracker.app.ui.components.ValidationTextField
import com.renttracker.app.ui.components.Spinner
import com.renttracker.app.ui.viewmodel.BuildingViewModel
import com.renttracker.app.ui.viewmodel.OwnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetailScreen(
    viewModel: BuildingViewModel,
    ownerViewModel: OwnerViewModel,
    buildingId: Long?,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedPropertyType by remember { mutableStateOf(PropertyType.RESIDENTIAL) }
    var notes by remember { mutableStateOf("") }
    var selectedOwnerId by remember { mutableStateOf<Long?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var existingBuilding by remember { mutableStateOf<Building?>(null) }
    
    var nameError by remember { mutableStateOf(false) }
    var ownerError by remember { mutableStateOf(false) }

    val owners by ownerViewModel.owners.collectAsState()

    // Load building data for editing
    LaunchedEffect(buildingId) {
        buildingId?.let {
            val building = viewModel.getBuildingById(it)
            building?.let { b ->
                existingBuilding = b
                name = b.name
                address = b.address ?: ""
                selectedPropertyType = b.propertyType
                notes = b.notes ?: ""
                selectedOwnerId = b.ownerId
            }
        }
    }

    Scaffold(
        topBar = {
            RentTrackerTopBar(
                title = if (buildingId == null) "Add Building" else "Edit Building",
                onNavigationClick = onNavigateBack,
                actions = {
                    if (buildingId != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                    IconButton(onClick = {
                        nameError = name.isBlank()
                        ownerError = selectedOwnerId == null

                        if (!nameError && !ownerError) {
                            val building = Building(
                                id = buildingId ?: 0,
                                name = name,
                                address = address.ifBlank { null },
                                propertyType = selectedPropertyType,
                                notes = notes.ifBlank { null },
                                ownerId = selectedOwnerId!!
                            )
                            if (buildingId == null) {
                                viewModel.insertBuilding(building) { onNavigateBack() }
                            } else {
                                viewModel.updateBuilding(building) { onNavigateBack() }
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
                label = "Building Name",
                isRequired = true,
                isError = nameError,
                errorMessage = "Name is required"
            )

            // Owner Selection
            if (owners.isNotEmpty()) {
                Column {
                    Spinner(
                        label = "Owner *",
                        items = owners,
                        selectedItem = owners.find { it.id == selectedOwnerId } ?: owners.first(),
                        onItemSelected = { 
                            selectedOwnerId = it.id
                            ownerError = false
                        },
                        itemToString = { it.name },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (ownerError) {
                        Text(
                            text = "Owner is required",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Please add an owner first",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Property Type Selection
            Spinner(
                label = "Property Type *",
                items = PropertyType.values().toList(),
                selectedItem = selectedPropertyType,
                onItemSelected = { selectedPropertyType = it },
                itemToString = { it.name },
                modifier = Modifier.fillMaxWidth()
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

        if (showDeleteDialog && existingBuilding != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Building") },
                text = { Text("Are you sure you want to delete this building?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteBuilding(existingBuilding!!) { onNavigateBack() }
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
