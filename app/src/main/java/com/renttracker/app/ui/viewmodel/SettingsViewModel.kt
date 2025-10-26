package com.renttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    val currency: StateFlow<String> = preferencesManager.currencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    val appLock: StateFlow<Boolean> = preferencesManager.appLockFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val paymentMethods: StateFlow<List<String>> = preferencesManager.paymentMethodsFlow
        .stateIn(
            viewModelScope, 
            SharingStarted.WhileSubscribed(5000), 
            PreferencesManager.DEFAULT_PAYMENT_METHODS.split(",").map { it.trim() }
        )

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            preferencesManager.setCurrency(currency)
        }
    }

    fun setAppLock(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAppLock(enabled)
        }
    }

    fun setPaymentMethods(methods: List<String>) {
        viewModelScope.launch {
            preferencesManager.setPaymentMethods(methods)
        }
    }
}
