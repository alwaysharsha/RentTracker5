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
            
            // First, force database to sync and log current database state
            Log.d(TAG, "Forcing database sync before backup...")
            try {
                // Force database to write any pending changes
                val writableDb = database.openHelper.writableDatabase
                writableDb.execSQL("PRAGMA synchronous = FULL")
                writableDb.execSQL("PRAGMA wal_checkpoint(FULL)")
                Log.d(TAG, "Database sync completed")
            } catch (e: Exception) {
                Log.w(TAG, "Could not force database sync: ${e.message}")
            }
            
            logCurrentDatabaseState(database, preferencesManager)
            
            // Create backup
            Log.d(TAG, "Creating test backup...")
            val backupManager = SQLiteBackupManager(context, database, preferencesManager)
            val backupUri = backupManager.createBackup()
            
            if (backupUri == null) {
                Log.e(TAG, "Backup creation failed - returned null URI")
                return false
            }
            
            Log.d(TAG, "Backup created successfully: $backupUri")
            
            // Check if backup file is accessible
            try {
                context.contentResolver.openInputStream(backupUri)?.use { input ->
                    val availableBytes = input.available()
                    Log.d(TAG, "Backup file is accessible, $availableBytes bytes available")
                } ?: run {
                    Log.e(TAG, "Cannot access backup file - input stream is null")
                    return false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to access backup file", e)
                return false
            }
            
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
                    Log.d(TAG, "✅ BACKUP TEST PASSED")
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
            Log.e(TAG, "❌ BACKUP TEST FAILED: Exception during backup test", e)
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
            
            // Check database file existence
            val dbPath = database.openHelper.writableDatabase.path ?: "unknown"
            val dbFileObj = File(dbPath)
            Log.d(TAG, "Database file path: $dbPath")
            Log.d(TAG, "Database file exists: ${dbFileObj.exists()}")
            Log.d(TAG, "Database file size: ${dbFileObj.length()} bytes")
            Log.d(TAG, "Database file readable: ${dbFileObj.canRead()}")
            
            // Force database to be opened and check if it's actually writable
            try {
                val writableDb = database.openHelper.writableDatabase
                Log.d(TAG, "Database is writable: ${writableDb.isOpen}")
                Log.d(TAG, "Database version: ${writableDb.version}")
                Log.d(TAG, "Database path from writable: ${writableDb.path}")
            } catch (e: Exception) {
                Log.e(TAG, "Error accessing writable database", e)
            }
            
            // Get actual data counts with detailed logging
            try {
                Log.d(TAG, "Querying owners table...")
                val owners = database.ownerDao().getAllOwners().first()
                Log.d(TAG, "Owners count: ${owners.size}")
                if (owners.isNotEmpty()) {
                    Log.d(TAG, "Sample owner: ${owners.first()}")
                }
                
                Log.d(TAG, "Querying buildings table...")
                val buildings = database.buildingDao().getAllBuildings().first()
                Log.d(TAG, "Buildings count: ${buildings.size}")
                if (buildings.isNotEmpty()) {
                    Log.d(TAG, "Sample building: ${buildings.first()}")
                }
                
                Log.d(TAG, "Querying tenants table...")
                val tenants = database.tenantDao().getActiveTenants().first()
                Log.d(TAG, "Active tenants count: ${tenants.size}")
                if (tenants.isNotEmpty()) {
                    Log.d(TAG, "Sample tenant: ${tenants.first()}")
                }
                
                Log.d(TAG, "Querying payments table...")
                val payments = database.paymentDao().getAllPayments().first()
                Log.d(TAG, "Payments count: ${payments.size}")
                if (payments.isNotEmpty()) {
                    Log.d(TAG, "Sample payment: ${payments.first()}")
                }
                
                Log.d(TAG, "Querying documents table...")
                val documents = database.documentDao().getAllDocuments().first()
                Log.d(TAG, "Documents count: ${documents.size}")
                if (documents.isNotEmpty()) {
                    Log.d(TAG, "Sample document: ${documents.first()}")
                }
                
                // Check if there are any vendor or expense records
                try {
                    val vendors = database.vendorDao().getAllVendors().first()
                    Log.d(TAG, "Vendors count: ${vendors.size}")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not query vendors: ${e.message}")
                }
                
                try {
                    val expenses = database.expenseDao().getAllExpenses().first()
                    Log.d(TAG, "Expenses count: ${expenses.size}")
                } catch (e: Exception) {
                    Log.w(TAG, "Could not query expenses: ${e.message}")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error querying database tables", e)
            }
            
            // Log preferences
            try {
                val currency = preferencesManager.getCurrencySync()
                val appLock = preferencesManager.getAppLockSync()
                val paymentMethods = preferencesManager.getPaymentMethodsSync()
                
                Log.d(TAG, "Currency: $currency")
                Log.d(TAG, "App Lock: $appLock")
                Log.d(TAG, "Payment Methods: $paymentMethods")
            } catch (e: Exception) {
                Log.e(TAG, "Error reading preferences", e)
            }
            
            Log.d(TAG, "=== END DATABASE STATE ===")
        } catch (e: Exception) {
            Log.e(TAG, "Error in logCurrentDatabaseState", e)
        }
    }

}
