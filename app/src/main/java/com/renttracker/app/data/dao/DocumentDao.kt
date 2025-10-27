package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Document
import com.renttracker.app.data.model.EntityType
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY uploadDate DESC")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): Document?

    @Query("SELECT * FROM documents WHERE id = :id")
    fun getDocumentByIdFlow(id: Long): Flow<Document?>

    @Query("SELECT * FROM documents WHERE entityType = :entityType AND entityId = :entityId ORDER BY uploadDate DESC")
    fun getDocumentsByEntity(entityType: EntityType, entityId: Long): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE entityType = :entityType AND entityId = :entityId ORDER BY uploadDate DESC")
    suspend fun getDocumentsByEntitySync(entityType: EntityType, entityId: Long): List<Document>

    @Query("SELECT COUNT(*) FROM documents WHERE entityType = :entityType AND entityId = :entityId")
    fun getDocumentCountByEntity(entityType: EntityType, entityId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long

    @Update
    suspend fun updateDocument(document: Document)

    @Delete
    suspend fun deleteDocument(document: Document)

    @Query("DELETE FROM documents WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteDocumentsByEntity(entityType: EntityType, entityId: Long)
}
