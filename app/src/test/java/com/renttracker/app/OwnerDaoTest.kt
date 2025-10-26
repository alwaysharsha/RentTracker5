package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.model.Owner
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OwnerDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RentTrackerDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RentTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetOwner() = runTest {
        val owner = Owner(
            name = "John Doe",
            email = "john@example.com",
            mobile = "+1234567890"
        )
        
        val id = database.ownerDao().insertOwner(owner)
        val retrieved = database.ownerDao().getOwnerById(id)
        
        assertNotNull(retrieved)
        assertEquals("John Doe", retrieved?.name)
        assertEquals("john@example.com", retrieved?.email)
    }

    @Test
    fun getAllOwners() = runTest {
        val owner1 = Owner(name = "John Doe", email = "john@example.com", mobile = "+1234567890")
        val owner2 = Owner(name = "Jane Smith", email = "jane@example.com", mobile = "+0987654321")
        
        database.ownerDao().insertOwner(owner1)
        database.ownerDao().insertOwner(owner2)
        
        val owners = database.ownerDao().getAllOwners().first()
        
        assertEquals(2, owners.size)
    }

    @Test
    fun updateOwner() = runTest {
        val owner = Owner(name = "John Doe", email = "john@example.com", mobile = "+1234567890")
        val id = database.ownerDao().insertOwner(owner)
        
        val updatedOwner = owner.copy(id = id, name = "John Updated")
        database.ownerDao().updateOwner(updatedOwner)
        
        val retrieved = database.ownerDao().getOwnerById(id)
        assertEquals("John Updated", retrieved?.name)
    }

    @Test
    fun deleteOwner() = runTest {
        val owner = Owner(name = "John Doe", email = "john@example.com", mobile = "+1234567890")
        val id = database.ownerDao().insertOwner(owner)
        
        val insertedOwner = database.ownerDao().getOwnerById(id)!!
        database.ownerDao().deleteOwner(insertedOwner)
        
        val retrieved = database.ownerDao().getOwnerById(id)
        assertNull(retrieved)
    }
}
