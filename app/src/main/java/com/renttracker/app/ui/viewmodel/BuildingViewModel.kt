package com.renttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.model.Building
import com.renttracker.app.data.model.BuildingWithOwner
import com.renttracker.app.data.repository.RentTrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BuildingViewModel(private val repository: RentTrackerRepository) : ViewModel() {
    val buildings: StateFlow<List<Building>> = repository.getAllBuildings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val buildingsWithOwner: StateFlow<List<BuildingWithOwner>> = repository.getAllBuildingsWithOwner()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getBuildingById(id: Long): Building? {
        return repository.getBuildingById(id)
    }

    fun insertBuilding(building: Building, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertBuilding(building)
            onComplete(id)
        }
    }

    fun updateBuilding(building: Building, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateBuilding(building)
            onComplete()
        }
    }

    fun deleteBuilding(building: Building, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteBuilding(building)
            onComplete()
        }
    }
}
