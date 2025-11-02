package com.renttracker.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.renttracker.app.MainActivity
import com.renttracker.app.data.model.Document
import com.renttracker.app.data.model.EntityType
import com.renttracker.app.ui.viewmodel.DocumentViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documentViewModel: DocumentViewModel,
    onNavigateBack: () -> Unit,
    mainActivity: MainActivity
) {
    val context = LocalContext.current
    val documents by documentViewModel.allDocuments.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var documentToDelete by remember { mutableStateOf<Document?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var documentToEdit by remember { mutableStateOf<Document?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documents") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showUploadDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Document")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showUploadDialog = true }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Document")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Total Documents",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = documents.size.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Files",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = documentViewModel.getTotalStorageUsed(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Storage Used",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Documents List
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Documents",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Tap the + button to upload your first document",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documents, key = { it.id }) { document ->
                        DocumentCard(
                            document = document,
                            dateFormat = dateFormat,
                            documentViewModel = documentViewModel,
                            onDelete = {
                                documentToDelete = document
                                showDeleteDialog = true
                            },
                            onEdit = {
                                documentToEdit = document
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && documentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Document") },
            text = { Text("Are you sure you want to delete '${documentToDelete!!.documentName}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        documentViewModel.deleteDocument(documentToDelete!!) {
                            showDeleteDialog = false
                            documentToDelete = null
                        }
                    }
                ) {
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
    
    // Upload Document Dialog
    if (showUploadDialog) {
        var documentName by remember { mutableStateOf("") }
        var selectedEntityType by remember { mutableStateOf(EntityType.TENANT) }
        var notes by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Upload Document") },
            text = {
                Column {
                    // Document Name
                    OutlinedTextField(
                        value = documentName,
                        onValueChange = { documentName = it },
                        label = { Text("Document Name (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Entity Type
                    Text(
                        text = "Entity Type",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = when (selectedEntityType) {
                                EntityType.OWNER -> "Owner Document"
                                EntityType.BUILDING -> "Building Document"
                                EntityType.TENANT -> "Tenant Document"
                                EntityType.PAYMENT -> "Payment Document"
                            },
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            EntityType.values().forEach { entityType ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (entityType) {
                                                EntityType.OWNER -> "Owner Document"
                                                EntityType.BUILDING -> "Building Document"
                                                EntityType.TENANT -> "Tenant Document"
                                                EntityType.PAYMENT -> "Payment Document"
                                            }
                                        )
                                    },
                                    onClick = {
                                        selectedEntityType = entityType
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Upload Options
                    Text(
                        text = "Choose upload method:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // File picker button
                        OutlinedButton(
                            onClick = {
                                try {
                                    mainActivity.launchDocumentFilePicker(
                                        documentName.takeIf { it.isNotBlank() },
                                        selectedEntityType,
                                        notes.takeIf { it.isNotBlank() }
                                    )
                                    showUploadDialog = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    // Handle error gracefully
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("File")
                        }
                        
                        // Camera button
                        OutlinedButton(
                            onClick = {
                                try {
                                    when {
                                        ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED -> {
                                            mainActivity.launchDocumentCamera(
                                                documentName.takeIf { it.isNotBlank() },
                                                selectedEntityType,
                                                notes.takeIf { it.isNotBlank() }
                                            )
                                            showUploadDialog = false
                                        }
                                        else -> {
                                            showUploadDialog = false
                                            showPermissionDialog = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    // Handle error gracefully
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Camera")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("Camera permission is required to take photos. Please grant camera permission in app settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            showPermissionDialog = false
                        } catch (e: Exception) {
                            e.printStackTrace()
                            showPermissionDialog = false
                        }
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Edit Document Dialog
    if (showEditDialog && documentToEdit != null) {
        var editedName by remember { mutableStateOf(documentToEdit?.documentName ?: "") }
        var editedNotes by remember { mutableStateOf(documentToEdit?.notes ?: "") }
        var selectedEntityType by remember { mutableStateOf(documentToEdit?.entityType ?: EntityType.OWNER) }
        
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Document") },
            text = {
                Column {
                    // Document Name
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Document Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Entity Type
                    Text(
                        text = "Entity Type",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    var editExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = editExpanded,
                        onExpandedChange = { editExpanded = !editExpanded }
                    ) {
                        OutlinedTextField(
                            value = when (selectedEntityType) {
                                EntityType.OWNER -> "Owner Document"
                                EntityType.BUILDING -> "Building Document"
                                EntityType.TENANT -> "Tenant Document"
                                EntityType.PAYMENT -> "Payment Document"
                            },
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = editExpanded,
                            onDismissRequest = { editExpanded = false }
                        ) {
                            EntityType.values().forEach { entityType ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            when (entityType) {
                                                EntityType.OWNER -> "Owner Document"
                                                EntityType.BUILDING -> "Building Document"
                                                EntityType.TENANT -> "Tenant Document"
                                                EntityType.PAYMENT -> "Payment Document"
                                            }
                                        )
                                    },
                                    onClick = {
                                        selectedEntityType = entityType
                                        editExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Notes
                    OutlinedTextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        documentToEdit?.let { document ->
                            documentViewModel.updateDocument(
                                document.copy(
                                    documentName = editedName,
                                    entityType = selectedEntityType,
                                    notes = editedNotes.takeIf { it.isNotBlank() }
                                )
                            )
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DocumentCard(
    document: Document,
    dateFormat: SimpleDateFormat,
    documentViewModel: DocumentViewModel,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document Type Icon
            Icon(
                imageVector = when (document.documentType.lowercase()) {
                    "pdf" -> Icons.Filled.PictureAsPdf
                    "jpg", "jpeg", "png", "gif" -> Icons.Filled.Image
                    else -> Icons.Filled.Description
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Document Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document.documentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${document.entityType.name} Document",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = dateFormat.format(Date(document.uploadDate)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${documentViewModel.formatFileSize(document.fileSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (!document.notes.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = document.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Action Buttons
            Row {
                // Edit Button
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Delete Button
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
