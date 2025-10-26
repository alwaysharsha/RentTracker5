package com.renttracker.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.ui.viewmodel.*

class ViewModelFactory(
    private val repository: RentTrackerRepository,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OwnerViewModel::class.java) -> {
                OwnerViewModel(repository) as T
            }
            modelClass.isAssignableFrom(BuildingViewModel::class.java) -> {
                BuildingViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TenantViewModel::class.java) -> {
                TenantViewModel(repository) as T
            }
            modelClass.isAssignableFrom(PaymentViewModel::class.java) -> {
                PaymentViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(preferencesManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
