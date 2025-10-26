package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Tenant
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE isCheckedOut = 0 ORDER BY name ASC")
    fun getActiveTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE isCheckedOut = 1 ORDER BY name ASC")
    fun getCheckedOutTenants(): Flow<List<Tenant>>

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
