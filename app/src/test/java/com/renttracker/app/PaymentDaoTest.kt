package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.model.*
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
class PaymentDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RentTrackerDatabase
    private var tenantId: Long = 0

    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RentTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
        
        val tenant = Tenant(name = "Test Tenant", mobile = "+1234567890")
        tenantId = database.tenantDao().insertTenant(tenant)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetPayment() = runTest {
        val payment = Payment(
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = tenantId
        )
        
        val id = database.paymentDao().insertPayment(payment)
        val retrieved = database.paymentDao().getPaymentById(id)
        
        assertNotNull(retrieved)
        assertEquals(1000.0, retrieved?.amount ?: 0.0, 0.01)
        assertEquals("UPI", retrieved?.paymentMethod)
    }

    @Test
    fun getPaymentsByTenant() = runTest {
        val payment1 = Payment(
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = tenantId
        )
        val payment2 = Payment(
            date = System.currentTimeMillis(),
            amount = 500.0,
            paymentMethod = "Cash",
            paymentType = PaymentStatus.PARTIAL,
            tenantId = tenantId
        )
        
        database.paymentDao().insertPayment(payment1)
        database.paymentDao().insertPayment(payment2)
        
        val payments = database.paymentDao().getPaymentsByTenant(tenantId).first()
        
        assertEquals(2, payments.size)
    }

    @Test
    fun getAllPayments() = runTest {
        val payment = Payment(
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = tenantId
        )
        
        database.paymentDao().insertPayment(payment)
        
        val payments = database.paymentDao().getAllPayments().first()
        
        assertTrue(payments.isNotEmpty())
    }

    @Test
    fun updatePayment() = runTest {
        val payment = Payment(
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = tenantId
        )
        val id = database.paymentDao().insertPayment(payment)
        
        val updatedPayment = payment.copy(id = id, amount = 1200.0)
        database.paymentDao().updatePayment(updatedPayment)
        
        val retrieved = database.paymentDao().getPaymentById(id)
        assertEquals(1200.0, retrieved?.amount ?: 0.0, 0.01)
    }

    @Test
    fun deletePayment() = runTest {
        val payment = Payment(
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = tenantId
        )
        val id = database.paymentDao().insertPayment(payment)
        
        val insertedPayment = database.paymentDao().getPaymentById(id)!!
        database.paymentDao().deletePayment(insertedPayment)
        
        val retrieved = database.paymentDao().getPaymentById(id)
        assertNull(retrieved)
    }
}
