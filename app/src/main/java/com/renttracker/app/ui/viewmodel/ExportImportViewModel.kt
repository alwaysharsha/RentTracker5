package com.renttracker.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.data.utils.DataExportImportManager
import com.renttracker.app.data.utils.SQLiteBackupManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExportImportViewModel(
    private val repository: RentTrackerRepository,
    private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val database: RentTrackerDatabase
) : ViewModel() {

    private val dataManager = DataExportImportManager(context, repository, preferencesManager, database)

    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus

    private val _importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    val importStatus: StateFlow<ImportStatus> = _importStatus

    fun exportData(onComplete: (Uri?) -> Unit = {}) {
        viewModelScope.launch {
            try {
                _exportStatus.value = ExportStatus.Exporting
                val uri = dataManager.exportData()
                if (uri != null) {
                    _exportStatus.value = ExportStatus.Success(uri)
                    onComplete(uri)
                } else {
                    _exportStatus.value = ExportStatus.Error("Failed to export data")
                    onComplete(null)
                }
            } catch (e: Exception) {
                android.util.Log.e("ExportImportViewModel", "Exception during export", e)
                _exportStatus.value = ExportStatus.Error(e.message ?: "Unknown error")
                onComplete(null)
            }
        }
    }

    fun importData(uri: Uri, clearExisting: Boolean = false, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ExportImportViewModel", "Import requested with URI: $uri")
                android.util.Log.d("ExportImportViewModel", "URI scheme: ${uri.scheme}")
                android.util.Log.d("ExportImportViewModel", "URI authority: ${uri.authority}")
                android.util.Log.d("ExportImportViewModel", "URI path: ${uri.path}")
                android.util.Log.d("ExportImportViewModel", "URI toString: ${uri.toString()}")
                
                // Validate URI before proceeding
                if (uri.toString().isEmpty()) {
                    _importStatus.value = ImportStatus.Error("Invalid file selected")
                    onComplete(false)
                    return@launch
                }
                
                _importStatus.value = ImportStatus.Importing
                
                // FORCE ZIP IMPORT: If it's a ZIP file, bypass all validation and go directly to SQLite restore
                val uriString = uri.toString()
                val pathString = uri.path ?: ""
                val isZipFile = uriString.endsWith(".zip", ignoreCase = true) || 
                               pathString.endsWith(".zip", ignoreCase = true)
                
                android.util.Log.d("ExportImportViewModel", "Is ZIP file: $isZipFile")
                
                val success = if (isZipFile) {
                    android.util.Log.d("ExportImportViewModel", "Processing ZIP backup file")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            android.util.Log.d("ExportImportViewModel", "Creating SQLiteBackupManager instance")
                            val sqliteBackupManager = SQLiteBackupManager(context, database, preferencesManager)
                            android.util.Log.d("ExportImportViewModel", "Calling restoreFromBackup")
                            val result = sqliteBackupManager.restoreFromBackup(uri, clearExisting)
                            android.util.Log.d("ExportImportViewModel", "SQLite restore completed with result: $result")
                            
                            result
                        } catch (e: Exception) {
                            android.util.Log.e("ExportImportViewModel", "SQLite restore failed with exception", e)
                            android.util.Log.e("ExportImportViewModel", "Exception type: ${e::class.java.simpleName}")
                            android.util.Log.e("ExportImportViewModel", "Exception message: ${e.message}")
                            android.util.Log.e("ExportImportViewModel", "Exception stack trace: ${e.stackTraceToString()}")
                            false
                        }
                    }
                } else {
                    // Normal import for non-ZIP files
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            dataManager.importData(uri, clearExisting)
                        } catch (e: Exception) {
                            android.util.Log.e("ExportImportViewModel", "Import failed", e)
                            false
                        }
                    }
                }
                if (success) {
                    _importStatus.value = ImportStatus.Success
                    onComplete(true)
                } else {
                    // Provide more detailed error information
                    val fileName = uri.path?.substringAfterLast("/") ?: uri.toString()
                    val errorMessage = if (isZipFile) {
                        "Failed to restore ZIP backup: $fileName. The file may be corrupted or not a valid RentTracker backup. Check logs for details."
                    } else {
                        "Invalid file format: $fileName. Please select a valid RentTracker backup file (.zip or .json)."
                    }
                    _importStatus.value = ImportStatus.Error(errorMessage)
                    onComplete(false)
                }
            } catch (e: SecurityException) {
                android.util.Log.e("ExportImportViewModel", "Security exception during import", e)
                _importStatus.value = ImportStatus.Error("Permission denied. Cannot access the selected file.")
                onComplete(false)
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("ExportImportViewModel", "Invalid argument during import", e)
                android.util.Log.e("ExportImportViewModel", "Exception details: ${e::class.java.simpleName}: ${e.message}")
                android.util.Log.e("ExportImportViewModel", "Stack trace: ${e.stackTraceToString()}")
                _importStatus.value = ImportStatus.Error("Invalid file format. Please select a valid RentTracker backup file.")
                onComplete(false)
            } catch (e: Exception) {
                android.util.Log.e("ExportImportViewModel", "Exception during import", e)
                _importStatus.value = ImportStatus.Error("Import failed: ${e.message ?: "Unknown error"}")
                onComplete(false)
            }
        }
    }

    fun resetExportStatus() {
        _exportStatus.value = ExportStatus.Idle
    }

    fun resetImportStatus() {
        _importStatus.value = ImportStatus.Idle
    }

    sealed class ExportStatus {
        object Idle : ExportStatus()
        object Exporting : ExportStatus()
        data class Success(val uri: Uri) : ExportStatus()
        data class Error(val message: String) : ExportStatus()
    }

    sealed class ImportStatus {
        object Idle : ImportStatus()
        object Importing : ImportStatus()
        object Success : ImportStatus()
        data class Error(val message: String) : ImportStatus()
    }
}
