package com.renttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.model.EntityType
import com.renttracker.app.data.model.Tenant
import com.renttracker.app.data.model.TenantWithBuilding
import com.renttracker.app.data.repository.RentTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TenantViewModel(private val repository: RentTrackerRepository) : ViewModel() {
    val activeTenants: StateFlow<List<Tenant>> = repository.getActiveTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val activeTenantsWithBuilding: StateFlow<List<TenantWithBuilding>> = repository.getActiveTenantsWithBuilding()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val checkedOutTenants: StateFlow<List<Tenant>> = repository.getCheckedOutTenants()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val checkedOutTenantsWithBuilding: StateFlow<List<TenantWithBuilding>> = repository.getCheckedOutTenantsWithBuilding()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getTenantById(tenantId: Long): Flow<Tenant?> {
        return repository.getTenantByIdFlow(tenantId)
    }

    fun insertTenant(tenant: Tenant, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertTenant(tenant)
            onComplete(id)
        }
    }

    fun updateTenant(tenant: Tenant, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateTenant(tenant)
            onComplete()
        }
    }

    fun deleteTenant(tenant: Tenant, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            // Delete associated documents first
            repository.deleteDocumentsByEntity(EntityType.TENANT, tenant.id)
            // Then delete the tenant
            repository.deleteTenant(tenant)
            onComplete()
        }
    }

    fun checkoutTenant(tenant: Tenant, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateTenant(tenant.copy(isCheckedOut = true))
            onSuccess()
        }
    }
}
