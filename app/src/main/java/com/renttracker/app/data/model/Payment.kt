package com.renttracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Tenant::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tenantId")]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val amount: Double,
    val paymentMethod: String, // Changed from enum to String for customization
    val transactionDetails: String? = null,
    val paymentType: PaymentStatus,
    val pendingAmount: Double? = null, // Amount still pending for partial payments
    val notes: String? = null,
    val tenantId: Long,
    val rentMonth: Long // Rent month timestamp (MMM yyyy format - stored as first day of month)
)

enum class PaymentStatus {
    PARTIAL,
    FULL
}

fun Payment.isPendingPayment(): Boolean {
    return paymentType == PaymentStatus.PARTIAL && (pendingAmount ?: 0.0) > 0.0
}
