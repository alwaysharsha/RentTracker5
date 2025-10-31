package com.renttracker.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.renttracker.app.data.preferences.PreferencesManager
import com.renttracker.app.data.repository.RentTrackerRepository
import com.renttracker.app.ui.viewmodel.*

class ViewModelFactory(
    private val repository: RentTrackerRepository,
    private val preferencesManager: PreferencesManager,
    private val context: Context
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
            modelClass.isAssignableFrom(DocumentViewModel::class.java) -> {
                DocumentViewModel(repository, context) as T
            }
            modelClass.isAssignableFrom(VendorViewModel::class.java) -> {
                VendorViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExpenseViewModel::class.java) -> {
                ExpenseViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExportImportViewModel::class.java) -> {
                ExportImportViewModel(repository, context, preferencesManager) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
