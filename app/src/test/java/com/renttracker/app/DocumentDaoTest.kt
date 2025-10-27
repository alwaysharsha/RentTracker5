package com.renttracker.app

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.database.RentTrackerDatabase
import com.renttracker.app.data.model.Document
import com.renttracker.app.data.model.EntityType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DocumentDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RentTrackerDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RentTrackerDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetDocument() = runTest {
        val document = Document(
            documentName = "Test Document",
            documentType = "pdf",
            filePath = "/test/path/document.pdf",
            entityType = EntityType.OWNER,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L,
            mimeType = "application/pdf"
        )
        
        val id = database.documentDao().insertDocument(document)
        val retrieved = database.documentDao().getDocumentById(id)
        
        assertNotNull(retrieved)
        assertEquals("Test Document", retrieved?.documentName)
        assertEquals("pdf", retrieved?.documentType)
        assertEquals(EntityType.OWNER, retrieved?.entityType)
    }

    @Test
    fun getAllDocuments() = runTest {
        val doc1 = Document(
            documentName = "Document 1",
            documentType = "pdf",
            filePath = "/test/path/doc1.pdf",
            entityType = EntityType.OWNER,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        val doc2 = Document(
            documentName = "Document 2",
            documentType = "jpg",
            filePath = "/test/path/doc2.jpg",
            entityType = EntityType.TENANT,
            entityId = 2L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 2048L
        )
        
        database.documentDao().insertDocument(doc1)
        database.documentDao().insertDocument(doc2)
        
        val documents = database.documentDao().getAllDocuments().first()
        
        assertEquals(2, documents.size)
    }

    @Test
    fun getDocumentsByEntity() = runTest {
        val doc1 = Document(
            documentName = "Owner Doc 1",
            documentType = "pdf",
            filePath = "/test/path/owner1.pdf",
            entityType = EntityType.OWNER,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        val doc2 = Document(
            documentName = "Owner Doc 2",
            documentType = "jpg",
            filePath = "/test/path/owner2.jpg",
            entityType = EntityType.OWNER,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 2048L
        )
        val doc3 = Document(
            documentName = "Tenant Doc",
            documentType = "pdf",
            filePath = "/test/path/tenant.pdf",
            entityType = EntityType.TENANT,
            entityId = 2L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 3072L
        )
        
        database.documentDao().insertDocument(doc1)
        database.documentDao().insertDocument(doc2)
        database.documentDao().insertDocument(doc3)
        
        val ownerDocs = database.documentDao().getDocumentsByEntity(EntityType.OWNER, 1L).first()
        val tenantDocs = database.documentDao().getDocumentsByEntity(EntityType.TENANT, 2L).first()
        
        assertEquals(2, ownerDocs.size)
        assertEquals(1, tenantDocs.size)
        assertEquals("Tenant Doc", tenantDocs[0].documentName)
    }

    @Test
    fun getDocumentCountByEntity() = runTest {
        val doc1 = Document(
            documentName = "Doc 1",
            documentType = "pdf",
            filePath = "/test/path/doc1.pdf",
            entityType = EntityType.BUILDING,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        val doc2 = Document(
            documentName = "Doc 2",
            documentType = "pdf",
            filePath = "/test/path/doc2.pdf",
            entityType = EntityType.BUILDING,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        
        database.documentDao().insertDocument(doc1)
        database.documentDao().insertDocument(doc2)
        
        val count = database.documentDao().getDocumentCountByEntity(EntityType.BUILDING, 1L).first()
        
        assertEquals(2, count)
    }

    @Test
    fun updateDocument() = runTest {
        val document = Document(
            documentName = "Original Name",
            documentType = "pdf",
            filePath = "/test/path/document.pdf",
            entityType = EntityType.PAYMENT,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        val id = database.documentDao().insertDocument(document)
        
        val updatedDocument = document.copy(id = id, documentName = "Updated Name")
        database.documentDao().updateDocument(updatedDocument)
        
        val retrieved = database.documentDao().getDocumentById(id)
        assertEquals("Updated Name", retrieved?.documentName)
    }

    @Test
    fun deleteDocument() = runTest {
        val document = Document(
            documentName = "Test Document",
            documentType = "pdf",
            filePath = "/test/path/document.pdf",
            entityType = EntityType.OWNER,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        val id = database.documentDao().insertDocument(document)
        
        val insertedDoc = database.documentDao().getDocumentById(id)!!
        database.documentDao().deleteDocument(insertedDoc)
        
        val retrieved = database.documentDao().getDocumentById(id)
        assertNull(retrieved)
    }

    @Test
    fun deleteDocumentsByEntity() = runTest {
        val doc1 = Document(
            documentName = "Doc 1",
            documentType = "pdf",
            filePath = "/test/path/doc1.pdf",
            entityType = EntityType.TENANT,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        val doc2 = Document(
            documentName = "Doc 2",
            documentType = "pdf",
            filePath = "/test/path/doc2.pdf",
            entityType = EntityType.TENANT,
            entityId = 1L,
            uploadDate = System.currentTimeMillis(),
            fileSize = 1024L
        )
        
        database.documentDao().insertDocument(doc1)
        database.documentDao().insertDocument(doc2)
        
        database.documentDao().deleteDocumentsByEntity(EntityType.TENANT, 1L)
        
        val documents = database.documentDao().getDocumentsByEntity(EntityType.TENANT, 1L).first()
        assertEquals(0, documents.size)
    }

    @Test
    fun documentsOrderedByUploadDate() = runTest {
        val now = System.currentTimeMillis()
        val doc1 = Document(
            documentName = "Oldest",
            documentType = "pdf",
            filePath = "/test/path/doc1.pdf",
            entityType = EntityType.OWNER,
            entityId = 1L,
            uploadDate = now - 2000,
            fileSize = 1024L
        )
        val doc2 = Document(
            documentName = "Newest",
            documentType = "pdf",
            filePath = "/test/path/doc2.pdf",
            entityType = EntityType.OWNER,
            entityId = 1L,
            uploadDate = now,
            fileSize = 1024L
        )
        
        database.documentDao().insertDocument(doc1)
        database.documentDao().insertDocument(doc2)
        
        val documents = database.documentDao().getDocumentsByEntity(EntityType.OWNER, 1L).first()
        
        // Should be ordered newest first
        assertEquals("Newest", documents[0].documentName)
        assertEquals("Oldest", documents[1].documentName)
    }
}
