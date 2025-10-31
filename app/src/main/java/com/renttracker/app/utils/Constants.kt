package com.renttracker.app.utils

object Constants {
    
    // MIME Types
    const val MIME_TYPE_JSON = "application/json"
    const val MIME_TYPE_TEXT = "text/plain"
    const val MIME_TYPE_ANY = "*/*"
    
    // File Extensions
    const val FILE_EXTENSION_JSON = ".json"
    const val FILE_EXTENSION_TXT = ".txt"
    
    // Export/Import
    const val EXPORT_FILE_NAME_PREFIX = "renttracker_backup"
    const val EXPORT_DIR_NAME = "exports"
    const val BACKUP_FILE_CHOOSER_TITLE = "Select Backup File"
    const val SHARE_BACKUP_TITLE = "Share backup file"
    
    // Request Codes
    const val IMPORT_FILE_PICKER_REQUEST_CODE = 1001
    
    // Log Tags
    const val TAG_EXPORT_IMPORT = "ExportImportViewModel"
    const val TAG_DATA_MANAGER = "DataExportImportManager"
    const val TAG_SETTINGS = "SettingsScreen"
    const val TAG_MAIN_ACTIVITY = "MainActivity"
    
    // Error Messages
    const val ERROR_UNKNOWN = "Unknown error"
    const val ERROR_PERMISSION_DENIED = "Permission denied. Cannot access the selected file."
    const val ERROR_INVALID_FORMAT = "Invalid file format. Please select a valid RentTracker backup file."
    const val ERROR_IMPORT_FAILED = "Import failed"
    const val ERROR_EXPORT_FAILED = "Failed to export data"
    const val ERROR_NO_FILE_MANAGER = "No file manager app found. Please install a file manager."
    const val ERROR_FILE_PICKER_FAILED = "Error: Could not open file picker. Please try again."
    const val ERROR_GENERIC_PREFIX = "Error: "
    
    // Success Messages
    const val SUCCESS_IMPORT_COMPLETED = "Import completed successfully"
    const val SUCCESS_EXPORT_COMPLETED = "Export completed successfully"
    
    // Toast Durations
    const val TOAST_DURATION_SHORT = android.widget.Toast.LENGTH_SHORT
    const val TOAST_DURATION_LONG = android.widget.Toast.LENGTH_LONG
}
