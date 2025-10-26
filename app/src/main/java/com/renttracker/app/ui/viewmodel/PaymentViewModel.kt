package com.renttracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renttracker.app.data.model.Payment
import com.renttracker.app.data.repository.RentTrackerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentViewModel(private val repository: RentTrackerRepository) : ViewModel() {
    val allPayments: StateFlow<List<Payment>> = repository.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getPaymentsByTenant(tenantId: Long): StateFlow<List<Payment>> {
        return repository.getPaymentsByTenant(tenantId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    suspend fun getPaymentById(paymentId: Long): Payment? {
        return repository.getPaymentById(paymentId)
    }

    fun insertPayment(payment: Payment, onComplete: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertPayment(payment)
            onComplete(id)
        }
    }

    fun updatePayment(payment: Payment, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePayment(payment)
            onComplete()
        }
    }

    fun deletePayment(payment: Payment, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePayment(payment)
            onComplete()
        }
    }
}
