package com.renttracker.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.data.utils.DataExportImportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExportImportViewModel(
    private val repository: RentTrackerRepository,
    private val context: Context,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val dataManager = DataExportImportManager(context, repository, preferencesManager)

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
                // Validate URI before proceeding
                if (uri.toString().isEmpty()) {
                    _importStatus.value = ImportStatus.Error("Invalid file selected")
                    onComplete(false)
                    return@launch
                }
                
                _importStatus.value = ImportStatus.Importing
                val success = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    dataManager.importData(uri, clearExisting)
                }
                if (success) {
                    _importStatus.value = ImportStatus.Success
                    onComplete(true)
                } else {
                    _importStatus.value = ImportStatus.Error("Failed to import data. Please check if the file is a valid RentTracker backup.")
                    onComplete(false)
                }
            } catch (e: SecurityException) {
                android.util.Log.e("ExportImportViewModel", "Security exception during import", e)
                _importStatus.value = ImportStatus.Error("Permission denied. Cannot access the selected file.")
                onComplete(false)
            } catch (e: IllegalArgumentException) {
                android.util.Log.e("ExportImportViewModel", "Invalid argument during import", e)
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
