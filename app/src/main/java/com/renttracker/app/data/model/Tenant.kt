package com.renttracker.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tenants",
    foreignKeys = [
        ForeignKey(
            entity = Building::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("buildingId")]
)
data class Tenant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val email: String? = null,
    val mobile: String,
    val mobile2: String? = null,
    val familyMembers: String? = null,
    val buildingId: Long? = null,
    val startDate: Long? = null,
    val rentIncreaseDate: Long? = null,
    val rent: Double? = null,
    val securityDeposit: Double? = null,
    val checkoutDate: Long? = null,
    val isCheckedOut: Boolean = false,
    val notes: String? = null
)
