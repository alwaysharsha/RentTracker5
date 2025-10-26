package com.renttracker.app.data.repository

import com.renttracker.app.data.dao.*
import com.renttracker.app.data.model.*
import kotlinx.coroutines.flow.Flow

class RentTrackerRepository(
    private val ownerDao: OwnerDao,
    private val buildingDao: BuildingDao,
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao
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
    suspend fun getBuildingById(id: Long): Building? = buildingDao.getBuildingById(id)
    fun getBuildingsByOwner(ownerId: Long): Flow<List<Building>> = buildingDao.getBuildingsByOwner(ownerId)
    suspend fun insertBuilding(building: Building): Long = buildingDao.insertBuilding(building)
    suspend fun updateBuilding(building: Building) = buildingDao.updateBuilding(building)
    suspend fun deleteBuilding(building: Building) = buildingDao.deleteBuilding(building)

    // Tenant operations
    fun getActiveTenants(): Flow<List<Tenant>> = tenantDao.getActiveTenants()
    fun getCheckedOutTenants(): Flow<List<Tenant>> = tenantDao.getCheckedOutTenants()
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
}
