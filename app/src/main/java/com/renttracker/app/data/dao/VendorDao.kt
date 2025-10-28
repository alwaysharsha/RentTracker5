package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Vendor
import com.renttracker.app.data.model.VendorCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<Vendor>>

    @Query("SELECT * FROM vendors WHERE id = :id")
    suspend fun getVendorById(id: Long): Vendor?

    @Query("SELECT * FROM vendors WHERE id = :id")
    fun getVendorByIdFlow(id: Long): Flow<Vendor?>

    @Query("SELECT * FROM vendors WHERE category = :category ORDER BY name ASC")
    fun getVendorsByCategory(category: VendorCategory): Flow<List<Vendor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: Vendor): Long

    @Update
    suspend fun updateVendor(vendor: Vendor)

    @Delete
    suspend fun deleteVendor(vendor: Vendor)
}
