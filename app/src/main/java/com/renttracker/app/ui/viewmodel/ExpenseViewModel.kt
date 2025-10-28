package com.renttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.model.Expense
import com.renttracker.app.data.model.ExpenseCategory
import com.renttracker.app.data.repository.RentTrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: RentTrackerRepository) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repository.getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getExpenseById(id: Long): Expense? {
        return repository.getExpenseById(id)
    }

    fun getExpensesByBuilding(buildingId: Long): StateFlow<List<Expense>> {
        return repository.getExpensesByBuilding(buildingId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getExpensesByVendor(vendorId: Long): StateFlow<List<Expense>> {
        return repository.getExpensesByVendor(vendorId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getExpensesByCategory(category: ExpenseCategory): StateFlow<List<Expense>> {
        return repository.getExpensesByCategory(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getExpensesByDateRange(startDate: Long, endDate: Long): StateFlow<List<Expense>> {
        return repository.getExpensesByDateRange(startDate, endDate)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun insertExpense(expense: Expense, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertExpense(expense)
            onComplete(id)
        }
    }

    fun updateExpense(expense: Expense, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            onComplete()
        }
    }

    fun deleteExpense(expense: Expense, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            onComplete()
        }
    }
}
