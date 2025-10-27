package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var preferencesManager: PreferencesManager

    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock the flows
        whenever(preferencesManager.currencyFlow).thenReturn(MutableStateFlow("USD"))
        whenever(preferencesManager.appLockFlow).thenReturn(MutableStateFlow(false))
        whenever(preferencesManager.paymentMethodsFlow).thenReturn(
            MutableStateFlow(listOf("UPI", "Cash", "Bank Transfer - Personal", "Bank Transfer - HUF", "Bank Transfer - Others"))
        )
        
        viewModel = SettingsViewModel(preferencesManager)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setCurrency should call preferencesManager`() = runTest {
        val newCurrency = "EUR"
        
        viewModel.setCurrency(newCurrency)
        advanceUntilIdle()
        
        verify(preferencesManager).setCurrency(newCurrency)
    }

    @Test
    fun `setAppLock should call preferencesManager`() = runTest {
        viewModel.setAppLock(true)
        advanceUntilIdle()
        
        verify(preferencesManager).setAppLock(true)
    }

    @Test
    fun `setPaymentMethods should call preferencesManager with correct order`() = runTest {
        val reorderedMethods = listOf("Cash", "UPI", "Bank Transfer")
        
        viewModel.setPaymentMethods(reorderedMethods)
        advanceUntilIdle()
        
        verify(preferencesManager).setPaymentMethods(reorderedMethods)
    }

    @Test
    fun `setPaymentMethods should preserve order after multiple reorders`() = runTest {
        val firstReorder = listOf("Bank Transfer", "Cash", "UPI")
        val secondReorder = listOf("UPI", "Bank Transfer", "Cash")
        
        viewModel.setPaymentMethods(firstReorder)
        advanceUntilIdle()
        verify(preferencesManager).setPaymentMethods(firstReorder)
        
        viewModel.setPaymentMethods(secondReorder)
        advanceUntilIdle()
        verify(preferencesManager).setPaymentMethods(secondReorder)
    }

    @Test
    fun `setPaymentMethods should handle adding new method`() = runTest {
        val methodsWithNewOne = listOf("UPI", "Cash", "Bank Transfer", "Check")
        
        viewModel.setPaymentMethods(methodsWithNewOne)
        advanceUntilIdle()
        
        verify(preferencesManager).setPaymentMethods(methodsWithNewOne)
    }

    @Test
    fun `setPaymentMethods should handle removing a method`() = runTest {
        val methodsAfterRemoval = listOf("UPI", "Cash")
        
        viewModel.setPaymentMethods(methodsAfterRemoval)
        advanceUntilIdle()
        
        verify(preferencesManager).setPaymentMethods(methodsAfterRemoval)
    }

    @Test
    fun `currency flow should emit initial value`() = runTest {
        val currency = viewModel.currency.value
        assertEquals("USD", currency)
    }

    @Test
    fun `appLock flow should emit initial value`() = runTest {
        val appLock = viewModel.appLock.value
        assertEquals(false, appLock)
    }

    @Test
    fun `paymentMethods flow should emit initial value`() = runTest {
        val methods = viewModel.paymentMethods.value
        assertEquals(listOf("UPI", "Cash", "Bank Transfer - Personal", "Bank Transfer - HUF", "Bank Transfer - Others"), methods)
    }
}
