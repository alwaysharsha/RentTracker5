package com.renttracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Expense entity for tracking property-related expenses
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Vendor::class,
            parentColumns = ["id"],
            childColumns = ["vendorId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Building::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("vendorId"), Index("buildingId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val date: Long,
    val category: ExpenseCategory,
    val vendorId: Long? = null,
    val buildingId: Long? = null,
    val paymentMethod: String? = null,
    val notes: String? = null,
    val receiptPath: String? = null
)

/**
 * Expense categories for property management
 */
enum class ExpenseCategory {
    MAINTENANCE,
    REPAIRS,
    UTILITIES,
    CLEANING,
    LANDSCAPING,
    PEST_CONTROL,
    INSURANCE,
    PROPERTY_TAX,
    HOA_FEES,
    APPLIANCES,
    RENOVATION,
    SUPPLIES,
    OTHER
}
