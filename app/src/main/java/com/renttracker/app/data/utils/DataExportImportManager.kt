package com.renttracker.app.data.utils

import android.content.Context
import android.net.Uri
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.model.*
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.data.repository.RentTrackerRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for exporting and importing data
 */
class DataExportImportManager(
    private val context: Context,
    private val repository: RentTrackerRepository,
    private val preferencesManager: PreferencesManager,
    private val database: RentTrackerDatabase
) {

    private val sqliteBackupManager = SQLiteBackupManager(context, database, preferencesManager)

    /**
     * Exports all data to SQLite backup format (ZIP file containing database and documents)
     * @return URI of the exported backup file, or null if export failed
     */
    suspend fun exportData(): Uri? {
        return try {
            sqliteBackupManager.createBackup()
        } catch (e: Exception) {
            android.util.Log.e("DataExportImportManager", "Exception during export", e)
            null
        }
    }

    /**
     * Legacy method for backward compatibility - exports to JSON format
     * @deprecated Use exportData() for SQLite backup
     */
    @Deprecated("Use SQLite backup instead")
    suspend fun exportDataToJson(): Uri? {
        return try {
            val exportData = JSONObject()
            exportData.put("version", 1)
            exportData.put("exportDate", System.currentTimeMillis())
            
            // Export Settings
            val settings = JSONObject()
            settings.put("currency", preferencesManager.currencyFlow.first())
            settings.put("appLock", preferencesManager.appLockFlow.first())
            settings.put("paymentMethods", preferencesManager.paymentMethodsFlow.first().joinToString(","))
            exportData.put("settings", settings)
            
            // Export Owners
            val owners = repository.getAllOwners().first()
            val ownersArray = JSONArray()
            owners.forEach { owner ->
                ownersArray.put(ownerToJson(owner))
            }
            exportData.put("owners", ownersArray)
            
            // Export Buildings
            val buildings = repository.getAllBuildings().first()
            val buildingsArray = JSONArray()
            buildings.forEach { building ->
                buildingsArray.put(buildingToJson(building))
            }
            exportData.put("buildings", buildingsArray)
            
            // Export Tenants
            val activeTenants = repository.getActiveTenants().first()
            val checkedOutTenants = repository.getCheckedOutTenants().first()
            val allTenants = activeTenants + checkedOutTenants
            val tenantsArray = JSONArray()
            allTenants.forEach { tenant ->
                tenantsArray.put(tenantToJson(tenant))
            }
            exportData.put("tenants", tenantsArray)
            
            // Export Payments
            val payments = repository.getAllPayments().first()
            val paymentsArray = JSONArray()
            payments.forEach { payment ->
                paymentsArray.put(paymentToJson(payment))
            }
            exportData.put("payments", paymentsArray)
            
            // Export Documents
            val documents = repository.getAllDocuments().first()
            val documentsArray = JSONArray()
            documents.forEach { document ->
                documentsArray.put(documentToJson(document))
            }
            exportData.put("documents", documentsArray)
            
            // Export Vendors
            val vendors = repository.getAllVendors().first()
            val vendorsArray = JSONArray()
            vendors.forEach { vendor ->
                vendorsArray.put(vendorToJson(vendor))
            }
            exportData.put("vendors", vendorsArray)
            
            // Export Expenses
            val expenses = repository.getAllExpenses().first()
            val expensesArray = JSONArray()
            expenses.forEach { expense ->
                expensesArray.put(expenseToJson(expense))
            }
            exportData.put("expenses", expensesArray)
            
            // Save to file
            val fileName = "RentTracker_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val jsonContent = exportData.toString(2).toByteArray()
            
            // Save to app-specific external storage
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val exportFile = File(exportDir, fileName)
            FileOutputStream(exportFile).use { output ->
                output.write(jsonContent)
            }
            
            // Return FileProvider URI
            try {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    exportFile
                )
            } catch (e: Exception) {
                // Fallback for test environments
                Uri.fromFile(exportFile)
            }
        } catch (e: Exception) {
            android.util.Log.e("DataExportImportManager", "Exception during JSON export", e)
            null
        }
    }


    /**
     * Imports data from backup file (SQLite ZIP or JSON)
     * @param uri URI of the backup file to import
     * @param clearExisting If true, clears existing data before import
     * @return True if import was successful, false otherwise
     */
    suspend fun importData(uri: Uri, clearExisting: Boolean = false): Boolean {
        return try {
            android.util.Log.d("DataExportImportManager", "Starting import process")
            
            // Validate URI
            if (uri.toString().isEmpty()) {
                android.util.Log.e("DataExportImportManager", "Empty URI provided")
                return false
            }
            
            // Check for ZIP format - be very lenient with detection
            val uriString = uri.toString()
            val pathString = uri.path ?: ""
            
            val isZipFile = uriString.endsWith(".zip", ignoreCase = true) || 
                           pathString.endsWith(".zip", ignoreCase = true)
            
            android.util.Log.d("DataExportImportManager", "URI: $uriString")
            android.util.Log.d("DataExportImportManager", "Path: $pathString")
            android.util.Log.d("DataExportImportManager", "Is ZIP file: $isZipFile")
            
            if (isZipFile) {
                android.util.Log.d("DataExportImportManager", "Detected ZIP file, forcing SQLite backup restore")
                // For ZIP files, directly call SQLite backup manager to bypass any MIME type issues
                return sqliteBackupManager.restoreFromBackup(uri, clearExisting)
            }
            
            // For non-ZIP files, use the normal detection logic
            val mimeType = try {
                context.contentResolver.getType(uri) ?: ""
            } catch (e: Exception) {
                android.util.Log.e("DataExportImportManager", "Failed to get MIME type", e)
                ""
            }
            
            var fileName = try {
                getFileName(uri) ?: ""
            } catch (e: Exception) {
                android.util.Log.e("DataExportImportManager", "Failed to get filename", e)
                ""
            }
            
            // Fallback: try to get filename from URI path
            if (fileName.isEmpty()) {
                fileName = pathString
                android.util.Log.d("DataExportImportManager", "Using fallback filename from URI path: $fileName")
            }
            
            android.util.Log.d("DataExportImportManager", "Importing file: $fileName")
            android.util.Log.d("DataExportImportManager", "MIME type: $mimeType")
            
            // Check for ZIP format with MIME types as backup
            val isZipByMimeType = mimeType == "application/zip" || 
                                 mimeType == "application/x-zip-compressed" ||
                                 fileName.endsWith(".zip", ignoreCase = true)
            
            if (isZipByMimeType) {
                android.util.Log.d("DataExportImportManager", "Detected SQLite backup (ZIP) by MIME type")
                // SQLite backup
                sqliteBackupManager.restoreFromBackup(uri, clearExisting)
            } else {
                android.util.Log.d("DataExportImportManager", "Detected JSON backup (legacy)")
                // JSON backup (legacy)
                importFromJson(uri, clearExisting)
            }
        } catch (e: SecurityException) {
            android.util.Log.e("DataExportImportManager", "Security exception during import", e)
            false
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("DataExportImportManager", "Illegal argument during import", e)
            android.util.Log.e("DataExportImportManager", "Exception details: ${e::class.java.simpleName}: ${e.message}")
            false
        } catch (e: Exception) {
            android.util.Log.e("DataExportImportManager", "Exception during import", e)
            false
        }
    }

    /**
     * Legacy method for importing JSON backup files
     */
    @Deprecated("Use SQLite backup instead")
    private suspend fun importFromJson(uri: Uri, clearExisting: Boolean = false): Boolean {
        return try {
            // Validate URI
            if (uri.toString().isEmpty()) {
                return false
            }
            
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return false
            
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            if (jsonString.isBlank()) {
                return false
            }
            
            val importData = JSONObject(jsonString)
            
            // Validate version
            val version = importData.optInt("version", 0)
            if (version != 1) {
                return false // Unsupported version
            }
            
            // Clear existing data if requested
            if (clearExisting) {
                try {
                    // Delete all data in reverse order to respect foreign key constraints
                    repository.getAllPayments().first().forEach { repository.deletePayment(it) }
                    repository.getAllDocuments().first().forEach { repository.deleteDocument(it) }
                    repository.getAllExpenses().first().forEach { repository.deleteExpense(it) }
                    repository.getActiveTenants().first().forEach { repository.deleteTenant(it) }
                    repository.getCheckedOutTenants().first().forEach { repository.deleteTenant(it) }
                    repository.getAllBuildings().first().forEach { repository.deleteBuilding(it) }
                    repository.getAllVendors().first().forEach { repository.deleteVendor(it) }
                    repository.getAllOwners().first().forEach { repository.deleteOwner(it) }
                } catch (e: Exception) {
                    android.util.Log.e("DataExportImportManager", "Failed to clear existing data", e)
                    // Continue with import even if clear fails
                }
            }
            
            // Import Settings (always import settings, even if not clearing data)
            val settingsObject = importData.optJSONObject("settings")
            settingsObject?.let { settings ->
                try {
                    // Import currency
                    val currency = settings.optString("currency", "USD")
                    preferencesManager.setCurrency(currency)
                    
                    // Import app lock setting
                    val appLock = settings.optBoolean("appLock", false)
                    preferencesManager.setAppLock(appLock)
                    
                    // Import payment methods
                    val paymentMethodsString = settings.optString("paymentMethods", "")
                    if (paymentMethodsString.isNotEmpty()) {
                        val paymentMethods = paymentMethodsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (paymentMethods.isNotEmpty()) {
                            preferencesManager.setPaymentMethods(paymentMethods)
                        } else {
                            // Do nothing if payment methods list is empty
                        }
                    } else {
                        // Do nothing if payment methods string is empty
                    }
                } catch (e: Exception) {
                    android.util.Log.e("DataExportImportManager", "Failed to import settings", e)
                    // Continue with import even if settings import fails
                }
            }
            
            // Map old IDs to new IDs for maintaining relationships
            val ownerIdMap = mutableMapOf<Long, Long>()
            val buildingIdMap = mutableMapOf<Long, Long>()
            val tenantIdMap = mutableMapOf<Long, Long>()
            val vendorIdMap = mutableMapOf<Long, Long>()
            
            // Import Owners
            val ownersArray = importData.optJSONArray("owners")
            ownersArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val ownerJson = it.getJSONObject(i)
                        val oldId = ownerJson.optLong("id", 0)
                        val owner = jsonToOwner(ownerJson).copy(id = 0) // Reset ID for auto-generation
                        val newId = repository.insertOwner(owner)
                        if (oldId > 0) {
                            ownerIdMap[oldId] = newId
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other owners
                        android.util.Log.e("DataExportImportManager", "Failed to import owner", e)
                    }
                }
            }
            
            // Import Buildings
            val buildingsArray = importData.optJSONArray("buildings")
            buildingsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val buildingJson = it.getJSONObject(i)
                        val oldId = buildingJson.optLong("id", 0)
                        val oldOwnerId = buildingJson.getLong("ownerId")
                        val building = jsonToBuilding(buildingJson).copy(
                            id = 0, // Reset ID for auto-generation
                            ownerId = ownerIdMap[oldOwnerId] ?: oldOwnerId // Map to new owner ID
                        )
                        val newId = repository.insertBuilding(building)
                        if (oldId > 0) {
                            buildingIdMap[oldId] = newId
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other buildings
                        android.util.Log.e("DataExportImportManager", "Failed to import building", e)
                    }
                }
            }
            
            // Import Tenants
            val tenantsArray = importData.optJSONArray("tenants")
            tenantsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val tenantJson = it.getJSONObject(i)
                        val oldId = tenantJson.optLong("id", 0)
                        val oldBuildingId = if (tenantJson.isNull("buildingId")) null else tenantJson.optLong("buildingId")
                        val tenant = jsonToTenant(tenantJson).copy(
                            id = 0, // Reset ID for auto-generation
                            buildingId = oldBuildingId?.let { buildingIdMap[it] ?: it } // Map to new building ID
                        )
                        val newId = repository.insertTenant(tenant)
                        if (oldId > 0) {
                            tenantIdMap[oldId] = newId
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other tenants
                        android.util.Log.e("DataExportImportManager", "Failed to import tenant", e)
                    }
                }
            }
            
            // Import Payments
            val paymentsArray = importData.optJSONArray("payments")
            paymentsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val paymentJson = it.getJSONObject(i)
                        val oldTenantId = paymentJson.getLong("tenantId")
                        val payment = jsonToPayment(paymentJson).copy(
                            id = 0, // Reset ID for auto-generation
                            tenantId = tenantIdMap[oldTenantId] ?: oldTenantId // Map to new tenant ID
                        )
                        repository.insertPayment(payment)
                    } catch (e: Exception) {
                        // Log error but continue with other payments
                        android.util.Log.e("DataExportImportManager", "Failed to import payment", e)
                    }
                }
            }
            
            // Import Documents
            val documentsArray = importData.optJSONArray("documents")
            documentsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val documentJson = it.getJSONObject(i)
                        val entityType = EntityType.valueOf(documentJson.getString("entityType"))
                        val oldEntityId = documentJson.getLong("entityId")
                        
                        // Map entity ID based on entity type
                        val newEntityId = when (entityType) {
                            EntityType.OWNER -> ownerIdMap[oldEntityId] ?: oldEntityId
                            EntityType.BUILDING -> buildingIdMap[oldEntityId] ?: oldEntityId
                            EntityType.TENANT -> tenantIdMap[oldEntityId] ?: oldEntityId
                            EntityType.PAYMENT -> oldEntityId // Payments are inserted without ID mapping
                        }
                        
                        val document = jsonToDocument(documentJson).copy(
                            id = 0, // Reset ID for auto-generation
                            entityId = newEntityId
                        )
                        repository.insertDocument(document)
                    } catch (e: Exception) {
                        // Log error but continue with other documents
                        android.util.Log.e("DataExportImportManager", "Failed to import document", e)
                    }
                }
            }
            
            // Import Vendors (optional - may not exist in older backups)
            val vendorsArray = importData.optJSONArray("vendors")
            vendorsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val vendorJson = it.getJSONObject(i)
                        val oldId = vendorJson.optLong("id", 0)
                        val vendor = jsonToVendor(vendorJson).copy(id = 0) // Reset ID for auto-generation
                        val newId = repository.insertVendor(vendor)
                        if (oldId > 0) {
                            vendorIdMap[oldId] = newId
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other vendors
                        android.util.Log.e("DataExportImportManager", "Failed to import vendor", e)
                    }
                }
            }
            
            // Import Expenses (optional - may not exist in older backups)
            val expensesArray = importData.optJSONArray("expenses")
            expensesArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val expenseJson = it.getJSONObject(i)
                        val oldVendorId = if (expenseJson.has("vendorId") && !expenseJson.isNull("vendorId")) 
                            expenseJson.getLong("vendorId") else null
                        val oldBuildingId = if (expenseJson.has("buildingId") && !expenseJson.isNull("buildingId")) 
                            expenseJson.getLong("buildingId") else null
                        
                        val expense = jsonToExpense(expenseJson).copy(
                            id = 0, // Reset ID for auto-generation
                            vendorId = oldVendorId?.let { vendorIdMap[it] ?: it },
                            buildingId = oldBuildingId?.let { buildingIdMap[it] ?: it }
                        )
                        repository.insertExpense(expense)
                    } catch (e: Exception) {
                        // Log error but continue with other expenses
                        android.util.Log.e("DataExportImportManager", "Failed to import expense", e)
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("DataExportImportManager", "Exception during JSON import", e)
            false
        }
    }

    /**
     * Helper method to get filename from URI
     */
    private fun getFileName(uri: Uri): String? {
        return try {
            android.util.Log.d("DataExportImportManager", "Extracting filename from URI: $uri")
            
            // Validate URI first
            if (uri.toString().isEmpty()) {
                android.util.Log.w("DataExportImportManager", "Empty URI provided to getFileName")
                return null
            }
            
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            }
            android.util.Log.d("DataExportImportManager", "Extracted filename: $fileName")
            fileName
        } catch (e: SecurityException) {
            android.util.Log.e("DataExportImportManager", "Security exception extracting filename", e)
            null
        } catch (e: IllegalArgumentException) {
            android.util.Log.e("DataExportImportManager", "Illegal argument extracting filename", e)
            null
        } catch (e: Exception) {
            android.util.Log.e("DataExportImportManager", "Error extracting filename", e)
            null
        }
    }

    // JSON conversion methods
    private fun ownerToJson(owner: Owner): JSONObject {
        return JSONObject().apply {
            put("id", owner.id)
            put("name", owner.name)
            put("email", owner.email)
            put("mobile", owner.mobile)
            put("mobile2", owner.mobile2)
            put("address", owner.address)
        }
    }

    private fun jsonToOwner(json: JSONObject): Owner {
        return Owner(
            id = json.optLong("id", 0),
            name = json.getString("name"),
            email = json.optString("email", "").ifEmpty { null },
            mobile = json.getString("mobile"),
            mobile2 = json.optString("mobile2", "").ifEmpty { null },
            address = json.optString("address", "").ifEmpty { null }
        )
    }

    private fun buildingToJson(building: Building): JSONObject {
        return JSONObject().apply {
            put("id", building.id)
            put("name", building.name)
            put("ownerId", building.ownerId)
            put("address", building.address)
            put("propertyType", building.propertyType.name)
            put("notes", building.notes)
        }
    }

    private fun jsonToBuilding(json: JSONObject): Building {
        return Building(
            id = json.optLong("id", 0),
            name = json.getString("name"),
            ownerId = json.getLong("ownerId"),
            address = json.optString("address", "").ifEmpty { null },
            propertyType = PropertyType.valueOf(json.getString("propertyType")),
            notes = json.optString("notes", "").ifEmpty { null }
        )
    }

    private fun tenantToJson(tenant: Tenant): JSONObject {
        return JSONObject().apply {
            put("id", tenant.id)
            put("name", tenant.name)
            put("email", tenant.email)
            put("mobile", tenant.mobile)
            put("mobile2", tenant.mobile2)
            put("familyMembers", tenant.familyMembers)
            put("buildingId", tenant.buildingId)
            put("startDate", tenant.startDate)
            put("checkoutDate", tenant.checkoutDate)
            put("rentIncreaseDate", tenant.rentIncreaseDate)
            put("rent", tenant.rent)
            put("securityDeposit", tenant.securityDeposit)
            put("notes", tenant.notes)
            put("isCheckedOut", tenant.isCheckedOut)
        }
    }

    private fun jsonToTenant(json: JSONObject): Tenant {
        return Tenant(
            id = json.optLong("id", 0),
            name = json.getString("name"),
            email = json.optString("email", "").ifEmpty { null },
            mobile = json.getString("mobile"),
            mobile2 = json.optString("mobile2", "").ifEmpty { null },
            familyMembers = json.optString("familyMembers", "").ifEmpty { null },
            buildingId = if (json.isNull("buildingId")) null else json.optLong("buildingId"),
            startDate = if (json.isNull("startDate")) null else json.optLong("startDate"),
            checkoutDate = if (json.isNull("checkoutDate")) null else json.optLong("checkoutDate", 0),
            rentIncreaseDate = if (json.isNull("rentIncreaseDate")) null else json.optLong("rentIncreaseDate", 0),
            rent = if (json.isNull("rent")) null else json.optDouble("rent"),
            securityDeposit = if (json.isNull("securityDeposit")) null else json.optDouble("securityDeposit"),
            notes = json.optString("notes", "").ifEmpty { null },
            isCheckedOut = json.getBoolean("isCheckedOut")
        )
    }

    private fun paymentToJson(payment: Payment): JSONObject {
        return JSONObject().apply {
            put("id", payment.id)
            put("tenantId", payment.tenantId)
            put("date", payment.date)
            put("amount", payment.amount)
            put("paymentMethod", payment.paymentMethod)
            put("transactionDetails", payment.transactionDetails)
            put("paymentType", payment.paymentType.name)
            put("pendingAmount", payment.pendingAmount)
            put("notes", payment.notes)
            put("rentMonth", payment.rentMonth)
        }
    }

    private fun jsonToPayment(json: JSONObject): Payment {
        return Payment(
            id = json.optLong("id", 0),
            tenantId = json.getLong("tenantId"),
            date = json.getLong("date"),
            amount = json.getDouble("amount"),
            paymentMethod = json.getString("paymentMethod"),
            transactionDetails = json.optString("transactionDetails", "").ifEmpty { null },
            paymentType = PaymentStatus.valueOf(json.getString("paymentType")),
            pendingAmount = if (json.isNull("pendingAmount")) null else json.optDouble("pendingAmount"),
            notes = json.optString("notes", "").ifEmpty { null },
            rentMonth = json.optLong("rentMonth", System.currentTimeMillis())
        )
    }

    private fun documentToJson(document: Document): JSONObject {
        return JSONObject().apply {
            put("id", document.id)
            put("documentName", document.documentName)
            put("documentType", document.documentType)
            put("filePath", document.filePath)
            put("entityType", document.entityType.name)
            put("entityId", document.entityId)
            put("uploadDate", document.uploadDate)
            put("fileSize", document.fileSize)
            put("mimeType", document.mimeType)
            put("notes", document.notes)
        }
    }

    private fun jsonToDocument(json: JSONObject): Document {
        return Document(
            id = json.optLong("id", 0),
            documentName = json.getString("documentName"),
            documentType = json.getString("documentType"),
            filePath = json.getString("filePath"),
            entityType = EntityType.valueOf(json.getString("entityType")),
            entityId = json.getLong("entityId"),
            uploadDate = json.getLong("uploadDate"),
            fileSize = json.getLong("fileSize"),
            mimeType = json.optString("mimeType", "").ifEmpty { null },
            notes = json.optString("notes", "").ifEmpty { null }
        )
    }

    private fun vendorToJson(vendor: Vendor): JSONObject {
        return JSONObject().apply {
            put("id", vendor.id)
            put("name", vendor.name)
            put("category", vendor.category.name)
            put("phone", vendor.phone)
            put("email", vendor.email)
            put("address", vendor.address)
            put("notes", vendor.notes)
        }
    }

    private fun jsonToVendor(json: JSONObject): Vendor {
        return Vendor(
            id = json.optLong("id", 0),
            name = json.getString("name"),
            category = VendorCategory.valueOf(json.getString("category")),
            phone = if (json.has("phone") && !json.isNull("phone")) json.getString("phone") else null,
            email = if (json.has("email") && !json.isNull("email")) json.getString("email") else null,
            address = if (json.has("address") && !json.isNull("address")) json.getString("address") else null,
            notes = if (json.has("notes") && !json.isNull("notes")) json.getString("notes") else null
        )
    }

    private fun expenseToJson(expense: Expense): JSONObject {
        return JSONObject().apply {
            put("id", expense.id)
            put("description", expense.description)
            put("amount", expense.amount)
            put("date", expense.date)
            put("category", expense.category.name)
            put("vendorId", expense.vendorId)
            put("buildingId", expense.buildingId)
            put("paymentMethod", expense.paymentMethod)
            put("notes", expense.notes)
            put("receiptPath", expense.receiptPath)
        }
    }

    private fun jsonToExpense(json: JSONObject): Expense {
        return Expense(
            id = json.optLong("id", 0),
            description = json.getString("description"),
            amount = json.getDouble("amount"),
            date = json.getLong("date"),
            category = ExpenseCategory.valueOf(json.getString("category")),
            vendorId = if (json.has("vendorId") && !json.isNull("vendorId")) json.getLong("vendorId") else null,
            buildingId = if (json.has("buildingId") && !json.isNull("buildingId")) json.getLong("buildingId") else null,
            paymentMethod = if (json.has("paymentMethod") && !json.isNull("paymentMethod")) json.getString("paymentMethod") else null,
            notes = if (json.has("notes") && !json.isNull("notes")) json.getString("notes") else null,
            receiptPath = if (json.has("receiptPath") && !json.isNull("receiptPath")) json.getString("receiptPath") else null
        )
    }
}
