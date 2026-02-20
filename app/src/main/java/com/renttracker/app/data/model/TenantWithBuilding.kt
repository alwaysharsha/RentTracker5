package com.renttracker.app.data.model

data class TenantWithBuilding(
    val id: Long,
    val name: String,
    val email: String?,
    val mobile: String,
    val mobile2: String?,
    val familyMembers: String?,
    val buildingId: Long?,
    val startDate: Long?,
    val rentIncreaseDate: Long?,
    val rent: Double?,
    val securityDeposit: Double?,
    val checkoutDate: Long?,
    val isCheckedOut: Boolean,
    val notes: String?,
    val buildingName: String?
)
