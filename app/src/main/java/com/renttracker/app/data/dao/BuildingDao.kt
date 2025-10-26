package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Building
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings ORDER BY name ASC")
    fun getAllBuildings(): Flow<List<Building>>

    @Query("SELECT * FROM buildings WHERE id = :id")
    suspend fun getBuildingById(id: Long): Building?

    @Query("SELECT * FROM buildings WHERE ownerId = :ownerId ORDER BY name ASC")
    fun getBuildingsByOwner(ownerId: Long): Flow<List<Building>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: Building): Long

    @Update
    suspend fun updateBuilding(building: Building)

    @Delete
    suspend fun deleteBuilding(building: Building)
}
