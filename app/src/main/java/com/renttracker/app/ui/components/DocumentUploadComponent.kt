package com.renttracker.app.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.renttracker.app.data.model.Document
import com.renttracker.app.data.model.EntityType
import com.renttracker.app.ui.viewmodel.DocumentViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadComponent(
    documentViewModel: DocumentViewModel,
    entityType: EntityType,
    entityId: Long,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var documents by remember { mutableStateOf<List<Document>>(emptyList()) }
    var showUploadDialog by remember { mutableStateOf(false) }
    
    // Collect documents for this entity
    LaunchedEffect(entityId) {
        documentViewModel.getDocumentsByEntity(entityType, entityId).collect { docs ->
            documents = docs
        }
    }
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            handleFileUpload(context, documentViewModel, fileUri, entityType, entityId)
        }
    }
    
    // Camera launcher
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            photoUri?.let { uri ->
                handleFileUpload(context, documentViewModel, uri, entityType, entityId)
            }
        }
    }
    
    // Permission launcher for camera
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoUri = createImageUri(context)
            photoUri?.let { uri ->
                cameraLauncher.launch(uri)
            }
        }
    }
    
    Column(modifier = modifier) {
        // Upload section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Documents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(
                onClick = { showUploadDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Document")
            }
        }
        
        // Documents list
        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No documents uploaded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(documents) { document ->
                    DocumentItem(
                        document = document,
                        onDelete = {
                            // Handle document deletion
                            // This would need to be implemented in DocumentViewModel
                        }
                    )
                }
            }
        }
    }
    
    // Upload dialog
    if (showUploadDialog) {
        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = { Text("Add Document") },
            text = {
                Column {
                    Text("Choose how you want to add a document:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // File picker button
                        OutlinedButton(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                                showUploadDialog = false
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
                                when {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        photoUri = createImageUri(context)
                                        photoUri?.let { uri ->
                                            cameraLauncher.launch(uri)
                                        }
                                        showUploadDialog = false
                                    }
                                    else -> {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
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
}

@Composable
private fun DocumentItem(
    document: Document,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.documentName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (!document.notes.isNullOrEmpty()) {
                    Text(
                        text = document.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Uploaded: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(document.uploadDate))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete document",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun handleFileUpload(
    context: Context,
    documentViewModel: DocumentViewModel,
    fileUri: Uri,
    entityType: EntityType,
    entityId: Long
) {
    val fileName = getFileName(context, fileUri) ?: "document_${System.currentTimeMillis()}"
    
    documentViewModel.uploadDocument(
        uri = fileUri,
        documentName = fileName,
        entityType = entityType,
        entityId = entityId,
        notes = null
    ) { success ->
        // Handle upload completion if needed
    }
}

private fun createImageUri(context: Context): Uri? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        val storageDir = File(context.cacheDir, "images")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val imageFile = File(storageDir, imageFileName)
        
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
