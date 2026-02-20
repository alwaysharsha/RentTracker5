package com.renttracker.app.data.repository

import com.renttracker.app.data.dao.*
import com.renttracker.app.data.model.*
import kotlinx.coroutines.flow.Flow

class RentTrackerRepository(
    private val ownerDao: OwnerDao,
    private val buildingDao: BuildingDao,
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao,
    private val documentDao: DocumentDao,
    private val vendorDao: VendorDao,
    private val expenseDao: ExpenseDao
) {
    // Owner operations
    fun getAllOwners(): Flow<List<Owner>> = ownerDao.getAllOwners()
    suspend fun getOwnerById(id: Long): Owner? = ownerDao.getOwnerById(id)
    fun getOwnerByIdFlow(id: Long): Flow<Owner?> = ownerDao.getOwnerByIdFlow(id)
    suspend fun insertOwner(owner: Owner): Long = ownerDao.insertOwner(owner)
    suspend fun updateOwner(owner: Owner) = ownerDao.updateOwner(owner)
    suspend fun deleteOwner(owner: Owner) = ownerDao.deleteOwner(owner)

    // Building operations
    fun getAllBuildings(): Flow<List<Building>> = buildingDao.getAllBuildings()
    fun getAllBuildingsWithOwner(): Flow<List<BuildingWithOwner>> = buildingDao.getAllBuildingsWithOwner()
    suspend fun getBuildingById(id: Long): Building? = buildingDao.getBuildingById(id)
    fun getBuildingsByOwner(ownerId: Long): Flow<List<Building>> = buildingDao.getBuildingsByOwner(ownerId)
    suspend fun insertBuilding(building: Building): Long = buildingDao.insertBuilding(building)
    suspend fun updateBuilding(building: Building) = buildingDao.updateBuilding(building)
    suspend fun deleteBuilding(building: Building) = buildingDao.deleteBuilding(building)

    // Tenant operations
    fun getActiveTenants(): Flow<List<Tenant>> = tenantDao.getActiveTenants()
    fun getActiveTenantsWithBuilding(): Flow<List<TenantWithBuilding>> = tenantDao.getActiveTenantsWithBuilding()
    fun getCheckedOutTenants(): Flow<List<Tenant>> = tenantDao.getCheckedOutTenants()
    fun getCheckedOutTenantsWithBuilding(): Flow<List<TenantWithBuilding>> = tenantDao.getCheckedOutTenantsWithBuilding()
    suspend fun getTenantById(id: Long): Tenant? = tenantDao.getTenantById(id)
    fun getTenantByIdFlow(id: Long): Flow<Tenant?> = tenantDao.getTenantByIdFlow(id)
    suspend fun insertTenant(tenant: Tenant): Long = tenantDao.insertTenant(tenant)
    suspend fun updateTenant(tenant: Tenant) = tenantDao.updateTenant(tenant)
    suspend fun deleteTenant(tenant: Tenant) = tenantDao.deleteTenant(tenant)

    // Payment operations
    fun getPaymentsByTenant(tenantId: Long): Flow<List<Payment>> = paymentDao.getPaymentsByTenant(tenantId)
    suspend fun getPaymentById(id: Long): Payment? = paymentDao.getPaymentById(id)
    fun getAllPayments(): Flow<List<Payment>> = paymentDao.getAllPayments()
    suspend fun insertPayment(payment: Payment): Long = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)

    // Document operations
    fun getAllDocuments(): Flow<List<Document>> = documentDao.getAllDocuments()
    suspend fun getDocumentById(id: Long): Document? = documentDao.getDocumentById(id)
    fun getDocumentByIdFlow(id: Long): Flow<Document?> = documentDao.getDocumentByIdFlow(id)
    fun getDocumentsByEntity(entityType: EntityType, entityId: Long): Flow<List<Document>> = 
        documentDao.getDocumentsByEntity(entityType, entityId)
    suspend fun getDocumentsByEntitySync(entityType: EntityType, entityId: Long): List<Document> = 
        documentDao.getDocumentsByEntitySync(entityType, entityId)
    fun getDocumentCountByEntity(entityType: EntityType, entityId: Long): Flow<Int> = 
        documentDao.getDocumentCountByEntity(entityType, entityId)
    suspend fun insertDocument(document: Document): Long = documentDao.insertDocument(document)
    suspend fun updateDocument(document: Document) = documentDao.updateDocument(document)
    suspend fun deleteDocument(document: Document) = documentDao.deleteDocument(document)
    suspend fun deleteDocumentsByEntity(entityType: EntityType, entityId: Long) = 
        documentDao.deleteDocumentsByEntity(entityType, entityId)

    // Vendor operations
    fun getAllVendors(): Flow<List<Vendor>> = vendorDao.getAllVendors()
    suspend fun getVendorById(id: Long): Vendor? = vendorDao.getVendorById(id)
    fun getVendorByIdFlow(id: Long): Flow<Vendor?> = vendorDao.getVendorByIdFlow(id)
    fun getVendorsByCategory(category: VendorCategory): Flow<List<Vendor>> = vendorDao.getVendorsByCategory(category)
    suspend fun insertVendor(vendor: Vendor): Long = vendorDao.insertVendor(vendor)
    suspend fun updateVendor(vendor: Vendor) = vendorDao.updateVendor(vendor)
    suspend fun deleteVendor(vendor: Vendor) = vendorDao.deleteVendor(vendor)

    // Expense operations
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()
    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getExpenseById(id)
    fun getExpenseByIdFlow(id: Long): Flow<Expense?> = expenseDao.getExpenseByIdFlow(id)
    fun getExpensesByBuilding(buildingId: Long): Flow<List<Expense>> = expenseDao.getExpensesByBuilding(buildingId)
    fun getExpensesByVendor(vendorId: Long): Flow<List<Expense>> = expenseDao.getExpensesByVendor(vendorId)
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> = expenseDao.getExpensesByCategory(category)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> = expenseDao.getExpensesByDateRange(startDate, endDate)
    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
}
