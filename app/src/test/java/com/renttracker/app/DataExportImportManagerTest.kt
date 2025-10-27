package com.renttracker.app

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.model.*
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.data.utils.DataExportImportManager
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
class DataExportImportManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RentTrackerDatabase
    private lateinit var repository: RentTrackerRepository
    private lateinit var dataManager: DataExportImportManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context,
            RentTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        
        repository = RentTrackerRepository(
            ownerDao = database.ownerDao(),
            buildingDao = database.buildingDao(),
            tenantDao = database.tenantDao(),
            paymentDao = database.paymentDao(),
            documentDao = database.documentDao()
        )
        
        dataManager = DataExportImportManager(context, repository)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun exportDataCreatesFile() = runTest {
        // Add test data
        val ownerId = repository.insertOwner(
            Owner(
                name = "Test Owner",
                email = "test@example.com",
                mobile = "+1234567890"
            )
        )
        
        val buildingId = repository.insertBuilding(
            Building(
                name = "Test Building",
                ownerId = ownerId,
                propertyType = PropertyType.RESIDENTIAL
            )
        )
        
        val tenantId = repository.insertTenant(
            Tenant(
                name = "Test Tenant",
                mobile = "+1234567890",
                buildingId = buildingId,
                startDate = System.currentTimeMillis(),
                rent = 1000.0,
                securityDeposit = 2000.0,
                isCheckedOut = false
            )
        )
        
        // Export data
        val uri = dataManager.exportData()
        
        assertNotNull(uri)
        assertTrue(uri.toString().contains("RentTracker_Backup"))
    }

    @Test
    fun exportDataIncludesAllEntities() = runTest {
        // Add test data
        val ownerId = repository.insertOwner(
            Owner(name = "Owner", mobile = "+1234567890")
        )
        
        val buildingId = repository.insertBuilding(
            Building(
                name = "Building",
                ownerId = ownerId,
                propertyType = PropertyType.COMMERCIAL
            )
        )
        
        val tenantId = repository.insertTenant(
            Tenant(
                name = "Tenant",
                mobile = "+1234567890",
                buildingId = buildingId,
                startDate = System.currentTimeMillis(),
                rent = 1500.0,
                securityDeposit = 3000.0,
                isCheckedOut = false
            )
        )
        
        val paymentId = repository.insertPayment(
            Payment(
                tenantId = tenantId,
                date = System.currentTimeMillis(),
                amount = 1500.0,
                paymentMethod = "Cash",
                paymentType = PaymentStatus.FULL
            )
        )
        
        val documentId = repository.insertDocument(
            Document(
                documentName = "Test Doc",
                documentType = "pdf",
                filePath = "/test/path",
                entityType = EntityType.OWNER,
                entityId = ownerId,
                uploadDate = System.currentTimeMillis(),
                fileSize = 1024L
            )
        )
        
        // Export data
        val uri = dataManager.exportData()
        assertNotNull(uri)
        
        // Verify counts
        assertEquals(1, repository.getAllOwners().first().size)
        assertEquals(1, repository.getAllBuildings().first().size)
        assertEquals(1, repository.getActiveTenants().first().size)
        assertEquals(1, repository.getAllPayments().first().size)
        assertEquals(1, repository.getAllDocuments().first().size)
    }

    @Test
    fun importDataRestoresAllEntities() = runTest {
        // First, export data with entities
        val ownerId = repository.insertOwner(
            Owner(name = "Original Owner", mobile = "+1234567890")
        )
        
        val buildingId = repository.insertBuilding(
            Building(
                name = "Original Building",
                ownerId = ownerId,
                propertyType = PropertyType.MIXED
            )
        )
        
        val uri = dataManager.exportData()
        assertNotNull(uri)
        
        // Clear database (simulate new installation)
        database.clearAllTables()
        
        // Import data
        val success = dataManager.importData(uri!!, clearExisting = false)
        
        assertTrue(success)
        
        // Verify data was restored
        val owners = repository.getAllOwners().first()
        val buildings = repository.getAllBuildings().first()
        
        assertTrue(owners.isNotEmpty())
        assertTrue(buildings.isNotEmpty())
        assertEquals("Original Owner", owners[0].name)
        assertEquals("Original Building", buildings[0].name)
    }

    @Test
    fun importNonExistentFileReturnsFalse() = runTest {
        val fakeUri = android.net.Uri.parse("file:///non/existent/file.json")
        val success = dataManager.importData(fakeUri, clearExisting = false)
        
        assertFalse(success)
    }

    @Test
    fun exportEmptyDatabaseSucceeds() = runTest {
        // Export with no data
        val uri = dataManager.exportData()
        
        assertNotNull(uri)
    }

    @Test
    fun importEmptyBackupSucceeds() = runTest {
        // Export empty database
        val uri = dataManager.exportData()
        assertNotNull(uri)
        
        // Import empty backup
        val success = dataManager.importData(uri!!, clearExisting = false)
        
        assertTrue(success)
    }
}
