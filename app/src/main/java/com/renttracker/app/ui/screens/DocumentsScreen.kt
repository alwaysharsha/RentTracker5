package com.renttracker.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.renttracker.app.MainActivity
import com.renttracker.app.data.model.Document
import com.renttracker.app.data.model.EntityType
import com.renttracker.app.ui.viewmodel.DocumentViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlin.math.max
import kotlin.math.min

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
    var showPreviewDialog by remember { mutableStateOf(false) }
    var documentToPreview by remember { mutableStateOf<Document?>(null) }

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
                            },
                            onPreview = {
                                documentToPreview = document
                                showPreviewDialog = true
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
    
    // Document Preview Dialog
    if (showPreviewDialog && documentToPreview != null) {
        DocumentPreviewDialog(
            document = documentToPreview!!,
            onDismiss = { 
                showPreviewDialog = false
                documentToPreview = null
            }
        )
    }
}

@Composable
fun DocumentPreviewDialog(
    document: Document,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val file = File(document.filePath)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = document.documentName,
                maxLines = 1
            )
        },
        text = {
            if (file.exists()) {
                when {
                    document.documentType.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> {
                        // Image Preview
                        ImagePreview(file = file)
                    }
                    document.documentType.lowercase() in listOf("txt", "log", "md") -> {
                        // Text Preview
                        TextPreview(file = file)
                    }
                    else -> {
                        // Unsupported preview type
                        Column(
                            modifier = Modifier.fillMaxWidth(),
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
                                text = "Preview not available for this file type",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "File type: ${document.documentType.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "File not found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ImagePreview(file: File) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    // Debug logging
    android.util.Log.d("ImagePreview", "Attempting to load image: ${file.absolutePath}")
    android.util.Log.d("ImagePreview", "File exists: ${file.exists()}")
    android.util.Log.d("ImagePreview", "File size: ${file.length()} bytes")
    android.util.Log.d("ImagePreview", "File readable: ${file.canRead()}")
    
    val bitmap = remember(file) {
        try {
            // Check if file exists and is readable first
            if (!file.exists() || !file.canRead()) {
                android.util.Log.e("ImagePreview", "File does not exist or is not readable")
                null
            } else {
                // Try multiple BitmapFactory options
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inSampleSize = 1
                }
                
                android.util.Log.d("ImagePreview", "Trying primary decode with ARGB_8888")
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                android.util.Log.d("ImagePreview", "Primary decode result: ${bitmap != null}")
                
                // If primary decode fails, try with more aggressive options
                if (bitmap == null) {
                    android.util.Log.d("ImagePreview", "Trying fallback decode with RGB_565")
                    val fallbackOptions = BitmapFactory.Options().apply {
                        inPreferredConfig = Bitmap.Config.RGB_565
                        inSampleSize = 2
                    }
                    val fallbackBitmap = BitmapFactory.decodeFile(file.absolutePath, fallbackOptions)
                    android.util.Log.d("ImagePreview", "Fallback decode result: ${fallbackBitmap != null}")
                    fallbackBitmap
                } else {
                    bitmap
                }
            }
        } catch (e: OutOfMemoryError) {
            android.util.Log.e("ImagePreview", "OutOfMemoryError: ${e.message}")
            // Handle OOM with aggressive sampling
            try {
                val oomOptions = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                    inSampleSize = 4
                }
                BitmapFactory.decodeFile(file.absolutePath, oomOptions)
            } catch (e2: Exception) {
                android.util.Log.e("ImagePreview", "OOM fallback failed: ${e2.message}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ImagePreview", "Exception loading image: ${e.message}")
            null
        }
    }
    
    if (bitmap != null) {
        android.util.Log.d("ImagePreview", "Successfully loaded bitmap: ${bitmap.width}x${bitmap.height}")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clip(MaterialTheme.shapes.medium)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = max(1f, min(scale * zoom, 3f))
                        val maxX = (size.width * (scale - 1)) / 2
                        val maxY = (size.height * (scale - 1)) / 2
                        offsetX = max(-maxX, min(offsetX + pan.x, maxX))
                        offsetY = max(-maxY, min(offsetY + pan.y, maxY))
                    }
                }
        ) {
            Image(
                painter = BitmapPainter(bitmap.asImageBitmap()),
                contentDescription = "Image preview",
                modifier = Modifier
                    .fillMaxSize()
                    .offset { androidx.compose.ui.unit.IntOffset(offsetX.toInt(), offsetY.toInt()) },
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Pinch to zoom, drag to pan",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        android.util.Log.d("ImagePreview", "Showing error state")
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.BrokenImage,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Could not load image",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "File: ${file.name}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Path: ${file.absolutePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Supported formats: JPG, PNG, GIF, BMP, WebP",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun TextPreview(file: File) {
    val text = remember(file) {
        try {
            file.readText().take(2000) // Limit to first 2000 characters
        } catch (e: Exception) {
            "Error reading file: ${e.message}"
        }
    }
    
    val fullText = remember(file) {
        try {
            file.readText()
        } catch (e: Exception) {
            ""
        }
    }
    
    if (text.startsWith("Error reading file:")) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            
            if (fullText.length > 2000) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "... (showing first 2000 characters)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun DocumentCard(
    document: Document,
    dateFormat: SimpleDateFormat,
    documentViewModel: DocumentViewModel,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() },
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
                    "jpg", "jpeg", "png", "gif", "bmp", "webp" -> Icons.Filled.Image
                    "txt", "log", "md" -> Icons.Filled.TextSnippet
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
                
                // Show preview availability
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        document.documentType.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> "📷 Image preview available"
                        document.documentType.lowercase() in listOf("txt", "log", "md") -> "📄 Text preview available"
                        else -> "📁 No preview available"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Action Buttons
            Row {
                // Preview Button
                IconButton(onClick = onPreview) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = "Preview",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
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
