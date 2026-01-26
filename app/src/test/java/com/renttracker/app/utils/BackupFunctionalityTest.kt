package com.renttracker.app.utils

import kotlinx.coroutines.runBlocking
import java.io.File

// Simple test to check backup functionality
fun main() {
    println("Testing backup functionality...")
    
    // Check if we can create a simple ZIP file as a test
    val testFile = File("test_backup.zip")
    
    try {
        // Create a simple ZIP file to test basic functionality
        java.util.zip.ZipOutputStream(java.io.FileOutputStream(testFile)).use { zipOut ->
            zipOut.putNextEntry(java.util.zip.ZipEntry("test.txt"))
            zipOut.write("test content".toByteArray())
            zipOut.closeEntry()
        }
        
        println("✅ Basic ZIP creation works")
        println("Created file: ${testFile.absolutePath}")
        println("File size: ${testFile.length()} bytes")
        
        // Clean up
        testFile.delete()
        println("✅ Test completed successfully")
        
    } catch (e: Exception) {
        println("❌ ZIP creation failed: ${e.message}")
        e.printStackTrace()
    }
}
