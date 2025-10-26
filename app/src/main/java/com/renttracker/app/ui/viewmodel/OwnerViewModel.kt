package com.renttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.model.Owner
import com.renttracker.app.data.repository.RentTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OwnerViewModel(private val repository: RentTrackerRepository) : ViewModel() {
    val owners: StateFlow<List<Owner>> = repository.getAllOwners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getOwnerById(ownerId: Long): Flow<Owner?> {
        return repository.getOwnerByIdFlow(ownerId)
    }

    fun insertOwner(owner: Owner, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertOwner(owner)
            onComplete(id)
        }
    }

    fun updateOwner(owner: Owner, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateOwner(owner)
            onComplete()
        }
    }

    fun deleteOwner(owner: Owner, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteOwner(owner)
            onComplete()
        }
    }
}
