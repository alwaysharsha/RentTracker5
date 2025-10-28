package com.renttracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Vendor entity for managing service providers and contractors
 */
@Entity(tableName = "vendors")
data class Vendor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: VendorCategory,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val notes: String? = null
)

/**
 * Vendor categories for property management
 */
enum class VendorCategory {
    PLUMBER,
    ELECTRICIAN,
    CARPENTER,
    PAINTER,
    CLEANER,
    GARDENER,
    SECURITY,
    PEST_CONTROL,
    APPLIANCE_REPAIR,
    GENERAL_CONTRACTOR,
    OTHER
}
