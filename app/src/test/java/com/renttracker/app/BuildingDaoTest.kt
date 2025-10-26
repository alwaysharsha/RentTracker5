package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.model.Building
import com.renttracker.app.data.model.Owner
import com.renttracker.app.data.model.PropertyType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BuildingDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RentTrackerDatabase
    private var ownerId: Long = 0

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RentTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        
        val owner = Owner(name = "Test Owner", email = "test@example.com", mobile = "+1234567890")
        ownerId = database.ownerDao().insertOwner(owner)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetBuilding() = runTest {
        val building = Building(
            name = "Test Building",
            propertyType = PropertyType.RESIDENTIAL,
            ownerId = ownerId
        )
        
        val id = database.buildingDao().insertBuilding(building)
        val retrieved = database.buildingDao().getBuildingById(id)
        
        assertNotNull(retrieved)
        assertEquals("Test Building", retrieved?.name)
        assertEquals(PropertyType.RESIDENTIAL, retrieved?.propertyType)
    }

    @Test
    fun getAllBuildings() = runTest {
        val building1 = Building(name = "Building 1", propertyType = PropertyType.RESIDENTIAL, ownerId = ownerId)
        val building2 = Building(name = "Building 2", propertyType = PropertyType.COMMERCIAL, ownerId = ownerId)
        
        database.buildingDao().insertBuilding(building1)
        database.buildingDao().insertBuilding(building2)
        
        val buildings = database.buildingDao().getAllBuildings().first()
        
        assertEquals(2, buildings.size)
    }

    @Test
    fun getBuildingsByOwner() = runTest {
        val building1 = Building(name = "Building 1", propertyType = PropertyType.RESIDENTIAL, ownerId = ownerId)
        database.buildingDao().insertBuilding(building1)
        
        val buildings = database.buildingDao().getBuildingsByOwner(ownerId).first()
        
        assertEquals(1, buildings.size)
        assertEquals("Building 1", buildings[0].name)
    }

    @Test
    fun updateBuilding() = runTest {
        val building = Building(name = "Test Building", propertyType = PropertyType.RESIDENTIAL, ownerId = ownerId)
        val id = database.buildingDao().insertBuilding(building)
        
        val updatedBuilding = building.copy(id = id, name = "Updated Building")
        database.buildingDao().updateBuilding(updatedBuilding)
        
        val retrieved = database.buildingDao().getBuildingById(id)
        assertEquals("Updated Building", retrieved?.name)
    }

    @Test
    fun deleteBuilding() = runTest {
        val building = Building(name = "Test Building", propertyType = PropertyType.RESIDENTIAL, ownerId = ownerId)
        val id = database.buildingDao().insertBuilding(building)
        
        val insertedBuilding = database.buildingDao().getBuildingById(id)!!
        database.buildingDao().deleteBuilding(insertedBuilding)
        
        val retrieved = database.buildingDao().getBuildingById(id)
        assertNull(retrieved)
    }
}
