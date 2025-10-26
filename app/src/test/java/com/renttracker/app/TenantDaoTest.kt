package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.model.Tenant
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
class TenantDaoTest {

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
    fun insertAndGetTenant() = runTest {
        val tenant = Tenant(
            name = "John Tenant",
            mobile = "+1234567890"
        )
        
        val id = database.tenantDao().insertTenant(tenant)
        val retrieved = database.tenantDao().getTenantById(id)
        
        assertNotNull(retrieved)
        assertEquals("John Tenant", retrieved?.name)
    }

    @Test
    fun getActiveTenants() = runTest {
        val tenant1 = Tenant(name = "Active Tenant", mobile = "+1234567890", isCheckedOut = false)
        val tenant2 = Tenant(name = "Checked Out Tenant", mobile = "+0987654321", isCheckedOut = true)
        
        database.tenantDao().insertTenant(tenant1)
        database.tenantDao().insertTenant(tenant2)
        
        val activeTenants = database.tenantDao().getActiveTenants().first()
        
        assertEquals(1, activeTenants.size)
        assertEquals("Active Tenant", activeTenants[0].name)
    }

    @Test
    fun getCheckedOutTenants() = runTest {
        val tenant1 = Tenant(name = "Active Tenant", mobile = "+1234567890", isCheckedOut = false)
        val tenant2 = Tenant(name = "Checked Out Tenant", mobile = "+0987654321", isCheckedOut = true)
        
        database.tenantDao().insertTenant(tenant1)
        database.tenantDao().insertTenant(tenant2)
        
        val checkedOutTenants = database.tenantDao().getCheckedOutTenants().first()
        
        assertEquals(1, checkedOutTenants.size)
        assertEquals("Checked Out Tenant", checkedOutTenants[0].name)
    }

    @Test
    fun updateTenant() = runTest {
        val tenant = Tenant(name = "John Tenant", mobile = "+1234567890")
        val id = database.tenantDao().insertTenant(tenant)
        
        val updatedTenant = tenant.copy(id = id, name = "John Updated")
        database.tenantDao().updateTenant(updatedTenant)
        
        val retrieved = database.tenantDao().getTenantById(id)
        assertEquals("John Updated", retrieved?.name)
    }

    @Test
    fun deleteTenant() = runTest {
        val tenant = Tenant(name = "John Tenant", mobile = "+1234567890")
        val id = database.tenantDao().insertTenant(tenant)
        
        val insertedTenant = database.tenantDao().getTenantById(id)!!
        database.tenantDao().deleteTenant(insertedTenant)
        
        val retrieved = database.tenantDao().getTenantById(id)
        assertNull(retrieved)
    }
}
