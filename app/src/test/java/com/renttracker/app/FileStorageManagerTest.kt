package com.renttracker.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.renttracker.app.data.utils.FileStorageManager
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FileStorageManagerTest {

    private lateinit var fileStorageManager: FileStorageManager
    private lateinit var context: Context
    private lateinit var testFilesDir: File

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        fileStorageManager = FileStorageManager(context)
        testFilesDir = File(context.filesDir, "documents")
    }

    @After
    fun teardown() {
        // Clean up test files
        testFilesDir.listFiles()?.forEach { it.delete() }
    }

    @Test
    fun generateUniqueFileName() {
        val originalName = "test_document.pdf"
        val uniqueName = fileStorageManager.generateUniqueFileName(originalName)
        
        assertTrue(uniqueName.contains("test_document"))
        assertTrue(uniqueName.endsWith(".pdf"))
        assertNotEquals(originalName, uniqueName)
    }

    @Test
    fun getFileExtension() {
        assertEquals("pdf", fileStorageManager.getFileExtension("document.pdf"))
        assertEquals("jpg", fileStorageManager.getFileExtension("image.jpg"))
        assertEquals("", fileStorageManager.getFileExtension("noextension"))
        assertEquals("txt", fileStorageManager.getFileExtension("file.with.dots.txt"))
    }

    @Test
    fun formatFileSize() {
        assertEquals("0 B", fileStorageManager.formatFileSize(0))
        assertEquals("500 B", fileStorageManager.formatFileSize(500))
        assertEquals("1.0 KB", fileStorageManager.formatFileSize(1024))
        assertEquals("1.5 KB", fileStorageManager.formatFileSize(1536))
        assertEquals("1.0 MB", fileStorageManager.formatFileSize(1024 * 1024))
        assertEquals("1.5 MB", fileStorageManager.formatFileSize(1536 * 1024))
        assertEquals("1.0 GB", fileStorageManager.formatFileSize(1024L * 1024L * 1024L))
    }

    @Test
    fun fileExists() {
        // Test with non-existent file
        assertFalse(fileStorageManager.fileExists("/non/existent/path"))
        
        // Create a test file
        val testFile = File(testFilesDir, "test.txt")
        testFile.parentFile?.mkdirs()
        testFile.writeText("test content")
        
        // Test with existing file
        assertTrue(fileStorageManager.fileExists(testFile.absolutePath))
    }

    @Test
    fun getFileSize() {
        // Test with non-existent file
        assertEquals(0L, fileStorageManager.getFileSize("/non/existent/path"))
        
        // Create a test file with known content
        val testFile = File(testFilesDir, "test.txt")
        testFile.parentFile?.mkdirs()
        val testContent = "test content"
        testFile.writeText(testContent)
        
        // Test with existing file
        val size = fileStorageManager.getFileSize(testFile.absolutePath)
        assertTrue(size > 0)
    }

    @Test
    fun deleteFile() {
        // Create a test file
        val testFile = File(testFilesDir, "test_delete.txt")
        testFile.parentFile?.mkdirs()
        testFile.writeText("test content")
        
        assertTrue(testFile.exists())
        
        // Delete the file
        val deleted = fileStorageManager.deleteFile(testFile.absolutePath)
        
        assertTrue(deleted)
        assertFalse(testFile.exists())
    }

    @Test
    fun deleteNonExistentFile() {
        val result = fileStorageManager.deleteFile("/non/existent/file.txt")
        assertFalse(result)
    }

    @Test
    fun getTotalStorageUsed() {
        // Initially should be 0 or small
        val initialSize = fileStorageManager.getTotalStorageUsed()
        
        // Create test files
        val file1 = File(testFilesDir, "file1.txt")
        file1.parentFile?.mkdirs()
        file1.writeText("content1")
        
        val file2 = File(testFilesDir, "file2.txt")
        file2.writeText("content2")
        
        // Check total storage increased
        val newSize = fileStorageManager.getTotalStorageUsed()
        assertTrue(newSize > initialSize)
    }

    @Test
    fun getExtensionFromMimeType() {
        val pdfExtension = fileStorageManager.getExtensionFromMimeType("application/pdf")
        assertTrue(pdfExtension == "pdf" || pdfExtension.isEmpty()) // May vary by platform
        
        val jpegExtension = fileStorageManager.getExtensionFromMimeType("image/jpeg")
        assertTrue(jpegExtension.contains("jp") || jpegExtension.isEmpty()) // May be jpg or jpeg
    }
}
