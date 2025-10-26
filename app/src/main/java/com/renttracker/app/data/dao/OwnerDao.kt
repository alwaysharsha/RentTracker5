package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Owner
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owners ORDER BY name ASC")
    fun getAllOwners(): Flow<List<Owner>>

    @Query("SELECT * FROM owners WHERE id = :id")
    suspend fun getOwnerById(id: Long): Owner?

    @Query("SELECT * FROM owners WHERE id = :id")
    fun getOwnerByIdFlow(id: Long): Flow<Owner?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(owner: Owner): Long

    @Update
    suspend fun updateOwner(owner: Owner)

    @Delete
    suspend fun deleteOwner(owner: Owner)
}
