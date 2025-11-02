package com.renttracker.app.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.data.utils.SQLiteBackupManager
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for testing backup creation and validation
 */
object BackupTestUtils {
    
    private const val TAG = "BackupTestUtils"
    
    /**
     * Creates a test backup and validates it immediately
     * Returns true if both creation and validation succeed
     */
    suspend fun createAndValidateTestBackup(
        context: Context,
        database: RentTrackerDatabase,
        preferencesManager: PreferencesManager
    ): Boolean {
        return try {
            Log.d(TAG, "=== STARTING BACKUP TEST ===")
            
            // Create backup
            Log.d(TAG, "Creating test backup...")
            val backupManager = SQLiteBackupManager(context, database, preferencesManager)
            val backupUri = backupManager.createBackup()
            
            if (backupUri == null) {
                Log.e(TAG, "Backup creation failed - returned null URI")
                return false
            }
            
            Log.d(TAG, "Backup created successfully: $backupUri")
            
            // Validate the created backup
            Log.d(TAG, "Validating created backup...")
            val isValid = validateBackupFile(context, backupUri)
            
            Log.d(TAG, "Backup validation result: $isValid")
            
            if (isValid) {
                // Test restore to ensure it works end-to-end
                Log.d(TAG, "Testing restore process...")
                val restoreSuccess = backupManager.restoreFromBackup(backupUri, false)
                Log.d(TAG, "Restore test result: $restoreSuccess")
                
                if (restoreSuccess) {
                    Log.d(TAG, "✅ BACKUP TEST COMPLETE: All tests passed!")
                    return true
                } else {
                    Log.e(TAG, "❌ BACKUP TEST FAILED: Restore test failed")
                    return false
                }
            } else {
                Log.e(TAG, "❌ BACKUP TEST FAILED: Backup validation failed")
                return false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during backup test", e)
            return false
        }
    }
    
    /**
     * Validates a backup file format and contents
     */
    private fun validateBackupFile(context: Context, backupUri: Uri): Boolean {
        return try {
            Log.d(TAG, "Validating backup file: $backupUri")
            
            // Copy to temp file for validation
            val tempFile = File(context.cacheDir, "temp_validation_backup.zip")
            context.contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val bytesCopied = input.copyTo(output)
                    Log.d(TAG, "Copied $bytesCopied bytes for validation")
                }
            } ?: run {
                Log.e(TAG, "Failed to open input stream for validation")
                return false
            }
            
            if (!tempFile.exists() || tempFile.length() == 0L) {
                Log.e(TAG, "Temp file is empty or does not exist")
                return false
            }
            
            Log.d(TAG, "Temp file size: ${tempFile.length()} bytes")
            
            // Validate ZIP structure
            java.util.zip.ZipFile(tempFile).use { zipFile ->
                val entries = zipFile.entries().asSequence().toList()
                Log.d(TAG, "ZIP file contains ${entries.size} entries")
                
                // Check for required files
                val hasMetadata = entries.any { it.name == "metadata.json" }
                val hasDatabase = entries.any { it.name == "renttracker_database.db" }
                
                Log.d(TAG, "Has metadata.json: $hasMetadata")
                Log.d(TAG, "Has renttracker_database.db: $hasDatabase")
                
                entries.forEach { entry ->
                    Log.d(TAG, "Entry: ${entry.name} (${entry.size} bytes)")
                }
                
                if (!hasMetadata) {
                    Log.e(TAG, "Missing required metadata.json file")
                    return false
                }
                
                if (!hasDatabase) {
                    Log.e(TAG, "Missing required renttracker_database.db file")
                    return false
                }
                
                // Validate metadata content
                val metadataEntry = entries.find { it.name == "metadata.json" }
                if (metadataEntry != null) {
                    zipFile.getInputStream(metadataEntry).use { input ->
                        val metadata = String(input.readBytes())
                        Log.d(TAG, "Metadata content: $metadata")
                        
                        val isValidJson = metadata.trim().startsWith("{") && metadata.trim().endsWith("}")
                        val hasVersion = metadata.contains("version")
                        val hasBackupDate = metadata.contains("backupDate")
                        
                        Log.d(TAG, "Valid JSON: $isValidJson")
                        Log.d(TAG, "Has version: $hasVersion")
                        Log.d(TAG, "Has backupDate: $hasBackupDate")
                        
                        if (!isValidJson || !hasVersion || !hasBackupDate) {
                            Log.e(TAG, "Invalid metadata format")
                            return false
                        }
                    }
                } else {
                    Log.e(TAG, "Metadata entry not found")
                    return false
                }
                
                // Validate database file is not empty
                val databaseEntry = entries.find { it.name == "renttracker_database.db" }
                if (databaseEntry != null && databaseEntry.size == 0L) {
                    Log.e(TAG, "Database file is empty")
                    return false
                }
                
                if (databaseEntry != null) {
                    Log.d(TAG, "Database file size: ${databaseEntry.size} bytes")
                }
            }
            
            // Clean up temp file
            tempFile.delete()
            
            Log.d(TAG, "✅ Backup file validation successful")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during backup validation", e)
            false
        }
    }
    
    /**
     * Test method to verify current database state
     */
    suspend fun logCurrentDatabaseState(
        database: RentTrackerDatabase,
        preferencesManager: PreferencesManager
    ) {
        try {
            Log.d(TAG, "=== CURRENT DATABASE STATE ===")
            
            val owners = database.ownerDao().getAllOwners().first()
            val buildings = database.buildingDao().getAllBuildings().first()
            val tenants = database.tenantDao().getActiveTenants().first()
            val payments = database.paymentDao().getAllPayments().first()
            
            Log.d(TAG, "Owners count: ${owners.size}")
            Log.d(TAG, "Buildings count: ${buildings.size}")
            Log.d(TAG, "Tenants count: ${tenants.size}")
            Log.d(TAG, "Payments count: ${payments.size}")
            
            Log.d(TAG, "Currency: ${preferencesManager.currencyFlow.first()}")
            Log.d(TAG, "App Lock: ${preferencesManager.appLockFlow.first()}")
            Log.d(TAG, "Payment Methods: ${preferencesManager.paymentMethodsFlow.first()}")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error logging database state", e)
        }
    }
}
