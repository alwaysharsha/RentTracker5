package com.renttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.model.Vendor
import com.renttracker.app.data.model.VendorCategory
import com.renttracker.app.data.repository.RentTrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VendorViewModel(private val repository: RentTrackerRepository) : ViewModel() {
    val vendors: StateFlow<List<Vendor>> = repository.getAllVendors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getVendorById(id: Long): Vendor? {
        return repository.getVendorById(id)
    }

    fun getVendorsByCategory(category: VendorCategory): StateFlow<List<Vendor>> {
        return repository.getVendorsByCategory(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun insertVendor(vendor: Vendor, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertVendor(vendor)
            onComplete(id)
        }
    }

    fun updateVendor(vendor: Vendor, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateVendor(vendor)
            onComplete()
        }
    }

    fun deleteVendor(vendor: Vendor, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteVendor(vendor)
            onComplete()
        }
    }
}
