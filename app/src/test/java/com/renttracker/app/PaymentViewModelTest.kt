package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.renttracker.app.data.model.Payment
import com.renttracker.app.data.model.PaymentStatus
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.ui.viewmodel.PaymentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: RentTrackerRepository

    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertPayment should call repository`() = runTest {
        val payment = Payment(
            id = 0L,
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = 1L,
            rentMonth = System.currentTimeMillis()
        )

        whenever(repository.insertPayment(payment)).thenReturn(1L)

        var completedId = 0L
        viewModel.insertPayment(payment) { id ->
            completedId = id
        }

        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).insertPayment(payment)
        assertEquals(1L, completedId)
    }

    @Test
    fun `updatePayment should call repository`() = runTest {
        val payment = Payment(
            id = 1L,
            date = System.currentTimeMillis(),
            amount = 1500.0,
            paymentMethod = "Cash",
            paymentType = PaymentStatus.FULL,
            tenantId = 1L,
            rentMonth = System.currentTimeMillis()
        )

        var completed = false
        viewModel.updatePayment(payment) {
            completed = true
        }

        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).updatePayment(payment)
        assert(completed)
    }

    @Test
    fun `deletePayment should call repository`() = runTest {
        val payment = Payment(
            id = 1L,
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = 1L,
            rentMonth = System.currentTimeMillis()
        )

        var completed = false
        viewModel.deletePayment(payment) {
            completed = true
        }

        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).deletePayment(payment)
        assert(completed)
    }

    @Test
    fun `getPaymentById should return payment from repository`() = runTest {
        val payment = Payment(
            id = 1L,
            date = System.currentTimeMillis(),
            amount = 1000.0,
            paymentMethod = "UPI",
            paymentType = PaymentStatus.FULL,
            tenantId = 1L,
            rentMonth = System.currentTimeMillis()
        )

        whenever(repository.getPaymentById(1L)).thenReturn(payment)

        val result = viewModel.getPaymentById(1L)

        assertEquals(payment, result)
        verify(repository).getPaymentById(1L)
    }
}
