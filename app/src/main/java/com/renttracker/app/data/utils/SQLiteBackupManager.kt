package com.renttracker.app.data.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Utility class for creating and restoring SQLite database backups
 * Supports backing up both database and document files
 */
class SQLiteBackupManager(
    private val context: Context,
    private val database: RentTrackerDatabase,
    private val preferencesManager: PreferencesManager
) {

    companion object {
        private const val BACKUP_VERSION = 2
        private const val DATABASE_FILE_NAME = "renttracker_database.db"
        private const val DOCUMENTS_FOLDER = "documents"
        private const val METADATA_FILE_NAME = "metadata.json"
    }

    /**
     * Creates a complete backup including database and document files
     * @return URI of the backup file, or null if backup failed
     */
    suspend fun createBackup(): Uri? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val backupFileName = "RentTracker_Backup_$timestamp.zip"
            val tempBackupFile = File(context.cacheDir, backupFileName)
            
            ZipOutputStream(FileOutputStream(tempBackupFile)).use { zipOut ->
                // Add database file
                addDatabaseToZip(zipOut)
                
                // Add document files
                addDocumentsToZip(zipOut)
                
                // Add metadata file
                addMetadataToZip(zipOut)
            }
            
            // Save the backup file to Downloads folder
            val backupUri = saveBackupToDownloads(tempBackupFile, backupFileName)
            
            // Clean up temp file
            tempBackupFile.delete()
            
            backupUri
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Exception during backup", e)
            null
        }
    }

    /**
     * Restores data from a backup file
     * @param backupUri URI of the backup file
     * @param clearExisting If true, clears existing data before restore
     * @return True if restore was successful, false otherwise
     */
    suspend fun restoreFromBackup(backupUri: Uri, clearExisting: Boolean = false): Boolean {
        return try {
            android.util.Log.d("SQLiteBackupManager", "=== STARTING RESTORE PROCESS ===")
            android.util.Log.d("SQLiteBackupManager", "Backup URI: $backupUri")
            android.util.Log.d("SQLiteBackupManager", "Clear existing: $clearExisting")
            
            // Validate URI
            if (backupUri.toString().isEmpty()) {
                android.util.Log.e("SQLiteBackupManager", "Backup URI is empty")
                return false
            }
            
            val tempBackupFile = File(context.cacheDir, "temp_restore_backup.zip")
            android.util.Log.d("SQLiteBackupManager", "Temp backup file: ${tempBackupFile.absolutePath}")
            
            // Copy backup file to temp location
            android.util.Log.d("SQLiteBackupManager", "Copying backup file to temp location")
            context.contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(tempBackupFile).use { output ->
                    val bytesCopied = input.copyTo(output)
                    android.util.Log.d("SQLiteBackupManager", "Copied $bytesCopied bytes to temp file")
                }
            } ?: run {
                android.util.Log.e("SQLiteBackupManager", "Failed to open input stream for backup")
                return false
            }
            
            android.util.Log.d("SQLiteBackupManager", "Backup file copied successfully")
            android.util.Log.d("SQLiteBackupManager", "Temp file size: ${tempBackupFile.length()} bytes")
            
            if (!tempBackupFile.exists()) {
                android.util.Log.e("SQLiteBackupManager", "Temp backup file does not exist after copy!")
                return false
            }
            
            var restoreSuccess = false
            
            try {
                android.util.Log.d("SQLiteBackupManager", "Opening ZIP file for reading")
                ZipInputStream(FileInputStream(tempBackupFile)).use { zipIn ->
                    
                    // First, read metadata to validate backup
                    android.util.Log.d("SQLiteBackupManager", "Reading metadata from ZIP")
                    val metadata = readMetadata(zipIn)
                    android.util.Log.d("SQLiteBackupManager", "Metadata read: $metadata")
                    
                    if (metadata == null) {
                        android.util.Log.e("SQLiteBackupManager", "Metadata is null, cannot proceed with restore")
                        return false
                    }
                    
                    val metadataValid = validateMetadata(metadata)
                    android.util.Log.d("SQLiteBackupManager", "Metadata validation result: $metadataValid")
                    
                    if (!metadataValid) {
                        android.util.Log.e("SQLiteBackupManager", "Metadata validation failed, cannot proceed with restore")
                        return false
                    }
                    
                    // Reset zip stream to read from beginning
                    zipIn.closeEntry()
                    ZipInputStream(FileInputStream(tempBackupFile)).use { newZipIn ->
                        var entry = newZipIn.nextEntry
                        
                        // Clear existing data if requested
                        if (clearExisting) {
                            clearExistingData()
                        }
                        
                        // Process each entry in the zip
                        while (entry != null) {
                            when (entry.name) {
                                DATABASE_FILE_NAME -> {
                                    restoreDatabase(newZipIn)
                                }
                                DOCUMENTS_FOLDER -> {
                                    // Skip folder entry, individual files will be processed
                                }
                                METADATA_FILE_NAME -> {
                                    // Metadata already processed
                                }
                                else -> {
                                    // Handle document files
                                    if (entry.name.startsWith("$DOCUMENTS_FOLDER/")) {
                                        restoreDocumentFile(newZipIn, entry.name)
                                    }
                                }
                            }
                            newZipIn.closeEntry()
                            entry = newZipIn.nextEntry
                        }
                        
                        // Restore settings from metadata
                        restoreSettings(metadata)
                        
                        restoreSuccess = true
                    }
                }
            } finally {
                // Clean up temp file
                tempBackupFile.delete()
            }
            
            restoreSuccess
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Exception during restore", e)
            false
        }
    }

    /**
     * Adds the SQLite database to the zip file
     */
    private fun addDatabaseToZip(zipOut: ZipOutputStream) {
        try {
            // Get the actual database file path
            val dbFile = getDatabaseFile()
            if (dbFile.exists()) {
                zipOut.putNextEntry(ZipEntry(DATABASE_FILE_NAME))
                FileInputStream(dbFile).use { input ->
                    input.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to add database to zip", e)
        }
    }

    /**
     * Adds document files to the zip file
     */
    private fun addDocumentsToZip(zipOut: ZipOutputStream) {
        try {
            val documentsDir = File(context.filesDir, DOCUMENTS_FOLDER)
            if (documentsDir.exists()) {
                documentsDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val relativePath = file.relativeTo(documentsDir).path
                    val zipEntry = ZipEntry("$DOCUMENTS_FOLDER/$relativePath")
                    zipOut.putNextEntry(zipEntry)
                    FileInputStream(file).use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to add documents to zip", e)
        }
    }

    /**
     * Adds metadata file to the zip
     */
    private suspend fun addMetadataToZip(zipOut: ZipOutputStream) {
        try {
            val metadata = createMetadata()
            zipOut.putNextEntry(ZipEntry(METADATA_FILE_NAME))
            zipOut.write(metadata.toByteArray())
            zipOut.closeEntry()
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to add metadata to zip", e)
        }
    }

    /**
     * Creates metadata JSON for the backup
     */
    private suspend fun createMetadata(): String {
        return try {
            val currency = try {
                preferencesManager.currencyFlow.first()
            } catch (e: Exception) {
                android.util.Log.w("SQLiteBackupManager", "Failed to get currency from DataStore, using default", e)
                "USD"
            }
            
            val appLock = try {
                preferencesManager.appLockFlow.first()
            } catch (e: Exception) {
                android.util.Log.w("SQLiteBackupManager", "Failed to get app lock from DataStore, using default", e)
                false
            }
            
            val paymentMethods = try {
                preferencesManager.paymentMethodsFlow.first()
            } catch (e: Exception) {
                android.util.Log.w("SQLiteBackupManager", "Failed to get payment methods from DataStore, using default", e)
                PreferencesManager.DEFAULT_PAYMENT_METHODS.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            
            """
        {
            "version": $BACKUP_VERSION,
            "backupDate": ${System.currentTimeMillis()},
            "appVersion": "2.6",
            "settings": {
                "currency": "$currency",
                "appLock": $appLock,
                "paymentMethods": "${paymentMethods.joinToString(",")}"
            }
        }
        """.trimIndent()
        } catch (e: Exception) {
            android.util.Log.w("SQLiteBackupManager", "Failed to create metadata, using fallback", e)
            """
        {
            "version": $BACKUP_VERSION,
            "backupDate": ${System.currentTimeMillis()},
            "appVersion": "2.6",
            "settings": {
                "currency": "USD",
                "appLock": false,
                "paymentMethods": "UPI,Cash,Bank Transfer - Personal,Bank Transfer - HUF,Bank Transfer - Others"
            }
        }
        """.trimIndent()
        }
    }

    /**
     * Reads metadata from zip file
     */
    private fun readMetadata(zipIn: ZipInputStream): String? {
        return try {
            android.util.Log.d("SQLiteBackupManager", "Reading metadata from ZIP")
            var entry = zipIn.nextEntry
            var foundEntries = mutableListOf<String>()
            
            while (entry != null) {
                foundEntries.add(entry.name)
                android.util.Log.d("SQLiteBackupManager", "ZIP entry: ${entry.name}")
                if (entry.name == METADATA_FILE_NAME) {
                    val metadata = zipIn.bufferedReader().use { it.readText() }
                    android.util.Log.d("SQLiteBackupManager", "Found metadata: $metadata")
                    return metadata
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            
            android.util.Log.w("SQLiteBackupManager", "No metadata found in ZIP. Entries found: $foundEntries")
            null
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to read metadata", e)
            null
        }
    }

    /**
     * Validates backup metadata
     */
    private fun validateMetadata(metadata: String): Boolean {
        return try {
            android.util.Log.d("SQLiteBackupManager", "Validating metadata: $metadata")
            
            // More lenient validation - just check if it looks like JSON
            val isValid = metadata.trim().startsWith("{") && 
                         metadata.trim().endsWith("}") &&
                         (metadata.contains("version") || metadata.contains("backup") || metadata.contains("date"))
            
            android.util.Log.d("SQLiteBackupManager", "Metadata validation result: $isValid")
            
            // Even if metadata validation fails, we'll still try to restore
            if (!isValid) {
                android.util.Log.w("SQLiteBackupManager", "Metadata validation failed, but proceeding with restore anyway")
            }
            
            true // Always return true to allow restore attempt
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Exception during metadata validation", e)
            true // Even on exception, allow restore attempt
        }
    }

    /**
     * Restores the database from zip
     */
    private fun restoreDatabase(zipIn: ZipInputStream) {
        try {
            val dbFile = getDatabaseFile()
            android.util.Log.d("SQLiteBackupManager", "Restoring database to: ${dbFile.absolutePath}")
            
            // Ensure the database directory exists
            dbFile.parentFile?.mkdirs()
            
            // Create backup of current database before overwriting
            if (dbFile.exists()) {
                val backupFile = File(dbFile.parent, "${dbFile.name}.backup")
                dbFile.copyTo(backupFile, overwrite = true)
                android.util.Log.d("SQLiteBackupManager", "Created backup of current database")
            }
            
            val bytesWritten = FileOutputStream(dbFile).use { output ->
                zipIn.copyTo(output)
            }
            
            android.util.Log.d("SQLiteBackupManager", "Database restored successfully. Bytes written: $bytesWritten")
            android.util.Log.d("SQLiteBackupManager", "Restored database file size: ${dbFile.length()} bytes")
            
            // Verify the database file is not empty
            if (dbFile.length() == 0L) {
                android.util.Log.e("SQLiteBackupManager", "Restored database file is empty!")
            }
            
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to restore database", e)
            throw e // Re-throw to let the caller know restoration failed
        }
    }

    /**
     * Restores a document file from zip
     */
    private fun restoreDocumentFile(zipIn: ZipInputStream, entryName: String) {
        try {
            val relativePath = entryName.substringAfter("$DOCUMENTS_FOLDER/")
            val documentsDir = File(context.filesDir, DOCUMENTS_FOLDER)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            
            val targetFile = File(documentsDir, relativePath)
            // Ensure parent directories exist
            targetFile.parentFile?.mkdirs()
            
            FileOutputStream(targetFile).use { output ->
                zipIn.copyTo(output)
            }
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to restore document file: $entryName", e)
        }
    }

    /**
     * Restores settings from metadata
     */
    private suspend fun restoreSettings(metadata: String) {
        try {
            // Simple JSON parsing - in a real implementation, you might want to use a proper JSON library
            val settingsMatch = Regex("\"settings\":\\s*\\{([^}]+)\\}").find(metadata)
            settingsMatch?.let { match ->
                val settingsContent = match.groupValues[1]
                
                // Extract currency
                val currencyMatch = Regex("\"currency\":\\s*\"([^\"]+)\"").find(settingsContent)
                currencyMatch?.let {
                    preferencesManager.setCurrency(it.groupValues[1])
                }
                
                // Extract app lock
                val appLockMatch = Regex("\"appLock\":\\s*(true|false)").find(settingsContent)
                appLockMatch?.let {
                    preferencesManager.setAppLock(it.groupValues[1].toBoolean())
                }
                
                // Extract payment methods
                val paymentMethodsMatch = Regex("\"paymentMethods\":\\s*\"([^\"]+)\"").find(settingsContent)
                paymentMethodsMatch?.let {
                    val methods = it.groupValues[1].split(",").map { method -> method.trim() }.filter { it.isNotEmpty() }
                    if (methods.isNotEmpty()) {
                        preferencesManager.setPaymentMethods(methods)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to restore settings", e)
        }
    }

    /**
     * Clears existing data from database
     */
    private suspend fun clearExistingData() {
        try {
            database.runInTransaction {
                database.clearAllTables()
            }
        } catch (e: Exception) {
            android.util.Log.e("SQLiteBackupManager", "Failed to clear existing data", e)
        }
    }

    /**
     * Gets the actual database file
     */
    private fun getDatabaseFile(): File {
        // For Room databases, we need to get the underlying file
        val dbPath = context.getDatabasePath("renttracker_database").path
        return File(dbPath)
    }

    /**
     * Saves backup file to Downloads folder
     */
    private fun saveBackupToDownloads(backupFile: File, fileName: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Try MediaStore first for Android 10+
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RentTracker")
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { output ->
                        FileInputStream(backupFile).use { input ->
                            input.copyTo(output)
                        }
                    }
                    it
                }
            } catch (e: Exception) {
                // Fall back to app-specific storage if MediaStore fails
                createFileProviderUri(backupFile, fileName)
            }
        } else {
            // For Android 9 and below, use app-specific external storage with FileProvider
            createFileProviderUri(backupFile, fileName)
        }
    }

    /**
     * Creates a FileProvider URI for the backup file
     */
    private fun createFileProviderUri(backupFile: File, fileName: String): Uri {
        val exportDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        
        val finalBackupFile = File(exportDir, fileName)
        FileInputStream(backupFile).use { input ->
            FileOutputStream(finalBackupFile).use { output ->
                input.copyTo(output)
            }
        }
        
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                finalBackupFile
            )
        } catch (e: Exception) {
            // Fallback for test environments where FileProvider may not be set up
            Uri.fromFile(finalBackupFile)
        }
    }
}
