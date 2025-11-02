package com.renttracker.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.model.Document
import com.renttracker.app.data.model.EntityType
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.data.utils.FileStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DocumentViewModel(
    private val repository: RentTrackerRepository,
    private val context: Context
) : ViewModel() {

    private val fileStorageManager = FileStorageManager(context)
    
    private val _uploadStatus = MutableStateFlow<UploadStatus>(UploadStatus.Idle)
    val uploadStatus: StateFlow<UploadStatus> = _uploadStatus

    val allDocuments: Flow<List<Document>> = repository.getAllDocuments()

    fun getDocumentById(documentId: Long): Flow<Document?> {
        return repository.getDocumentByIdFlow(documentId)
    }

    fun uploadDocument(
        uri: Uri,
        documentName: String,
        entityType: EntityType,
        entityId: Long,
        notes: String? = null,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _uploadStatus.value = UploadStatus.Uploading

                // Get MIME type and file extension
                val mimeType = fileStorageManager.getMimeType(uri)
                val extension = if (mimeType != null) {
                    fileStorageManager.getExtensionFromMimeType(mimeType)
                } else {
                    fileStorageManager.getFileExtension(documentName)
                }

                // Generate unique filename
                val uniqueFileName = fileStorageManager.generateUniqueFileName(documentName)

                // Save file
                val filePath = fileStorageManager.saveFile(uri, uniqueFileName)
                
                if (filePath != null) {
                    // Get file size
                    val fileSize = fileStorageManager.getFileSize(filePath)

                    // Create document record
                    val document = Document(
                        documentName = documentName,
                        documentType = extension,
                        filePath = filePath,
                        entityType = entityType,
                        entityId = entityId,
                        uploadDate = System.currentTimeMillis(),
                        fileSize = fileSize,
                        mimeType = mimeType,
                        notes = notes
                    )

                    repository.insertDocument(document)
                    _uploadStatus.value = UploadStatus.Success
                    onComplete(true)
                } else {
                    _uploadStatus.value = UploadStatus.Error("Failed to save file")
                    onComplete(false)
                }
            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus.Error(e.message ?: "Unknown error")
                onComplete(false)
            }
        }
    }

    fun deleteDocument(document: Document, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Delete file from storage
                fileStorageManager.deleteFile(document.filePath)
                
                // Delete document record from database
                repository.deleteDocument(document)
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateDocument(document: Document, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.updateDocument(document)
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTotalStorageUsed(): String {
        return fileStorageManager.formatFileSize(fileStorageManager.getTotalStorageUsed())
    }

    fun formatFileSize(bytes: Long): String {
        return fileStorageManager.formatFileSize(bytes)
    }

    fun resetUploadStatus() {
        _uploadStatus.value = UploadStatus.Idle
    }

    sealed class UploadStatus {
        object Idle : UploadStatus()
        object Uploading : UploadStatus()
        object Success : UploadStatus()
        data class Error(val message: String) : UploadStatus()
    }
}
