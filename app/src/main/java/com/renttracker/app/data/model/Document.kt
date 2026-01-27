package com.renttracker.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Document entity for storing document metadata
 */
@Entity(tableName = "documents")
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentName: String,
    val documentType: String, // pdf, image, etc.
    val filePath: String, // local storage path
    val entityType: EntityType, // Owner, Building, Tenant, Payment
    val entityId: Long, // ID of the related entity
    val uploadDate: Long, // timestamp
    val fileSize: Long, // in bytes
    val mimeType: String? = null,
    val notes: String? = null
)

/**
 * Enum representing the entity types that can have documents
 */
enum class EntityType {
    OWNER,
    BUILDING,
    TENANT,
    PAYMENT,
    EXPENSE
}
