package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.renttracker.app.data.model.Tenant
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.ui.viewmodel.TenantViewModel
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
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TenantViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var repository: RentTrackerRepository

    private lateinit var viewModel: TenantViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = TenantViewModel(repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `checkoutTenant should call onSuccess`() = runTest {
        val tenant = Tenant(
            id = 1L,
            name = "Test Tenant",
            mobile = "+1234567890"
        )

        var successCalled = false
        viewModel.checkoutTenant(
            tenant = tenant,
            onSuccess = { successCalled = true }
        )

        testDispatcher.scheduler.advanceUntilIdle()
        assert(successCalled)
    }
}
