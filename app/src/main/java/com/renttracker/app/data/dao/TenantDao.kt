package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Tenant
import com.renttracker.app.data.model.TenantWithBuilding
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE isCheckedOut = 0 ORDER BY name ASC")
    fun getActiveTenants(): Flow<List<Tenant>>
    
    @Query("""
        SELECT t.id, t.name, t.email, t.mobile, t.mobile2, t.familyMembers, 
               t.buildingId, t.startDate, t.rentIncreaseDate, t.rent, 
               t.securityDeposit, t.checkoutDate, t.isCheckedOut, t.notes, 
               b.name as buildingName
        FROM tenants t
        LEFT JOIN buildings b ON t.buildingId = b.id
        WHERE t.isCheckedOut = 0
        ORDER BY t.name ASC
    """)
    fun getActiveTenantsWithBuilding(): Flow<List<TenantWithBuilding>>

    @Query("SELECT * FROM tenants WHERE isCheckedOut = 1 ORDER BY name ASC")
    fun getCheckedOutTenants(): Flow<List<Tenant>>
    
    @Query("""
        SELECT t.id, t.name, t.email, t.mobile, t.mobile2, t.familyMembers, 
               t.buildingId, t.startDate, t.rentIncreaseDate, t.rent, 
               t.securityDeposit, t.checkoutDate, t.isCheckedOut, t.notes, 
               b.name as buildingName
        FROM tenants t
        LEFT JOIN buildings b ON t.buildingId = b.id
        WHERE t.isCheckedOut = 1
        ORDER BY t.name ASC
    """)
    fun getCheckedOutTenantsWithBuilding(): Flow<List<TenantWithBuilding>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getTenantById(id: Long): Tenant?

    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getTenantByIdFlow(id: Long): Flow<Tenant?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: Tenant): Long

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)
}
