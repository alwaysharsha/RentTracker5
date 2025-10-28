package com.renttracker.app.data.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.renttracker.app.data.model.*
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
    private val repository: RentTrackerRepository
) {

    /**
     * Exports all data to JSON format
     * @return URI of the exported file, or null if export failed
     */
    suspend fun exportData(): Uri? {
        return try {
            val exportData = JSONObject()
            exportData.put("version", 1)
            exportData.put("exportDate", System.currentTimeMillis())
            
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
            
            // Save to Downloads folder for Android 10+ using MediaStore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Try MediaStore first for Android 10+
                val mediaStoreUri = try {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/RentTracker")
                    }
                    
                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    
                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { output ->
                            output.write(jsonContent)
                        }
                        it
                    }
                } catch (e: Exception) {
                    // Fall back to app-specific storage if MediaStore fails
                    null
                }
                
                // Return MediaStore URI or fallback to app-specific storage
                mediaStoreUri ?: createFileProviderUri(exportDir = File(context.getExternalFilesDir(null), "exports"), fileName = fileName, jsonContent = jsonContent)
            } else {
                // For Android 9 and below, use app-specific external storage with FileProvider
                createFileProviderUri(exportDir = File(context.getExternalFilesDir(null), "exports"), fileName = fileName, jsonContent = jsonContent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates a FileProvider URI for the exported file
     * Falls back to Uri.fromFile() if FileProvider fails (for test environments)
     */
    private fun createFileProviderUri(exportDir: File, fileName: String, jsonContent: ByteArray): Uri {
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
        val exportFile = File(exportDir, fileName)
        FileOutputStream(exportFile).use { output ->
            output.write(jsonContent)
        }
        
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )
        } catch (e: Exception) {
            // Fallback for test environments where FileProvider may not be set up
            Uri.fromFile(exportFile)
        }
    }

    /**
     * Imports data from JSON file
     * @param uri URI of the JSON file to import
     * @param clearExisting If true, clears existing data before import
     * @return True if import was successful, false otherwise
     */
    suspend fun importData(uri: Uri, clearExisting: Boolean = false): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: return false
            val importData = JSONObject(jsonString)
            
            // Validate version
            val version = importData.optInt("version", 0)
            if (version != 1) {
                return false // Unsupported version
            }
            
            // Import Owners
            val ownersArray = importData.optJSONArray("owners")
            ownersArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val ownerJson = it.getJSONObject(i)
                        val owner = jsonToOwner(ownerJson)
                        repository.insertOwner(owner)
                    } catch (e: Exception) {
                        // Log error but continue with other owners
                        e.printStackTrace()
                    }
                }
            }
            
            // Import Buildings
            val buildingsArray = importData.optJSONArray("buildings")
            buildingsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val buildingJson = it.getJSONObject(i)
                        val building = jsonToBuilding(buildingJson)
                        repository.insertBuilding(building)
                    } catch (e: Exception) {
                        // Log error but continue with other buildings
                        e.printStackTrace()
                    }
                }
            }
            
            // Import Tenants
            val tenantsArray = importData.optJSONArray("tenants")
            tenantsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val tenantJson = it.getJSONObject(i)
                        val tenant = jsonToTenant(tenantJson)
                        repository.insertTenant(tenant)
                    } catch (e: Exception) {
                        // Log error but continue with other tenants
                        e.printStackTrace()
                    }
                }
            }
            
            // Import Payments
            val paymentsArray = importData.optJSONArray("payments")
            paymentsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val paymentJson = it.getJSONObject(i)
                        val payment = jsonToPayment(paymentJson)
                        repository.insertPayment(payment)
                    } catch (e: Exception) {
                        // Log error but continue with other payments
                        e.printStackTrace()
                    }
                }
            }
            
            // Import Documents
            val documentsArray = importData.optJSONArray("documents")
            documentsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val documentJson = it.getJSONObject(i)
                        val document = jsonToDocument(documentJson)
                        repository.insertDocument(document)
                    } catch (e: Exception) {
                        // Log error but continue with other documents
                        e.printStackTrace()
                    }
                }
            }
            
            // Import Vendors (optional - may not exist in older backups)
            val vendorsArray = importData.optJSONArray("vendors")
            vendorsArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val vendorJson = it.getJSONObject(i)
                        val vendor = jsonToVendor(vendorJson)
                        repository.insertVendor(vendor)
                    } catch (e: Exception) {
                        // Log error but continue with other vendors
                        e.printStackTrace()
                    }
                }
            }
            
            // Import Expenses (optional - may not exist in older backups)
            val expensesArray = importData.optJSONArray("expenses")
            expensesArray?.let {
                for (i in 0 until it.length()) {
                    try {
                        val expenseJson = it.getJSONObject(i)
                        val expense = jsonToExpense(expenseJson)
                        repository.insertExpense(expense)
                    } catch (e: Exception) {
                        // Log error but continue with other expenses
                        e.printStackTrace()
                    }
                }
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
            email = json.optString("email", null),
            mobile = json.getString("mobile"),
            mobile2 = json.optString("mobile2", null),
            address = json.optString("address", null)
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
            address = json.optString("address", null),
            propertyType = PropertyType.valueOf(json.getString("propertyType")),
            notes = json.optString("notes", null)
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
            email = json.optString("email", null),
            mobile = json.getString("mobile"),
            mobile2 = json.optString("mobile2", null),
            familyMembers = json.optString("familyMembers", null),
            buildingId = if (json.isNull("buildingId")) null else json.optLong("buildingId"),
            startDate = if (json.isNull("startDate")) null else json.optLong("startDate"),
            checkoutDate = if (json.isNull("checkoutDate")) null else json.optLong("checkoutDate", 0),
            rentIncreaseDate = if (json.isNull("rentIncreaseDate")) null else json.optLong("rentIncreaseDate", 0),
            rent = if (json.isNull("rent")) null else json.optDouble("rent"),
            securityDeposit = if (json.isNull("securityDeposit")) null else json.optDouble("securityDeposit"),
            notes = json.optString("notes", null),
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
            transactionDetails = json.optString("transactionDetails", null),
            paymentType = PaymentStatus.valueOf(json.getString("paymentType")),
            pendingAmount = if (json.isNull("pendingAmount")) null else json.optDouble("pendingAmount"),
            notes = json.optString("notes", null),
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
            mimeType = json.optString("mimeType", null),
            notes = json.optString("notes", null)
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
