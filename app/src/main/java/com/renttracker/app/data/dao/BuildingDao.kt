package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Building
import com.renttracker.app.data.model.BuildingWithOwner
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings ORDER BY name ASC")
    fun getAllBuildings(): Flow<List<Building>>
    
    @Query("""
        SELECT b.id, b.name, b.address, b.propertyType, b.notes, b.ownerId, o.name as ownerName
        FROM buildings b
        INNER JOIN owners o ON b.ownerId = o.id
        ORDER BY b.name ASC
    """)
    fun getAllBuildingsWithOwner(): Flow<List<BuildingWithOwner>>

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
