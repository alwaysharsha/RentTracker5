package com.renttracker.app.data.model

data class BuildingWithOwner(
    val id: Long,
    val name: String,
    val address: String?,
    val propertyType: PropertyType,
    val notes: String?,
    val ownerId: Long,
    val ownerName: String
)
