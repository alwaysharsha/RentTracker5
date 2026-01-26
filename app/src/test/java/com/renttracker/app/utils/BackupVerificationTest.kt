package com.renttracker.app.utils

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Test script to create a valid RentTracker backup file for testing
 * This simulates the exact format that the app should create and accept
 */
fun createTestBackupFile(): File {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val backupFileName = "RentTracker_Backup_$timestamp.zip"
    val testFile = File("test_backup_$timestamp.zip")
    
    ZipOutputStream(FileOutputStream(testFile)).use { zipOut ->
        // Add metadata file (must be first for proper validation)
        zipOut.putNextEntry(ZipEntry("metadata.json"))
        val metadata = """{
    "version": 2,
    "backupDate": ${System.currentTimeMillis()},
    "appVersion": "4.8.4",
    "settings": {
        "currency": "USD",
        "appLock": false,
        "paymentMethods": "Cash,Bank Transfer,Credit Card"
    }
}"""
        zipOut.write(metadata.toByteArray())
        zipOut.closeEntry()
        
        // Add a dummy database file
        zipOut.putNextEntry(ZipEntry("renttracker_database.db"))
        // This would normally be the actual SQLite database
        // For testing, we'll add some dummy content
        val dummyDbContent = "SQLite format 3\x00\x10\x00\x01\x01\x00@  \x00\x00\x00\x00"
        zipOut.write(dummyDbContent.toByteArray())
        zipOut.closeEntry()
        
        // Add a documents folder entry (even if empty)
        zipOut.putNextEntry(ZipEntry("documents/"))
        zipOut.closeEntry()
    }
    
    println("✅ Created test backup file: ${testFile.absolutePath}")
    println("📁 File size: ${testFile.length()} bytes")
    println("📋 Contents:")
    
    // Verify the ZIP file contents
    java.util.zip.ZipFile(testFile).use { zipFile ->
        zipFile.entries().asSequence().forEach { entry ->
            println("   - ${entry.name} (${entry.size} bytes)")
        }
    }
    
    return testFile
}

/**
 * Test script to validate a backup file format
 */
fun validateBackupFile(backupFile: File): Boolean {
    return try {
        println("\n🔍 Validating backup file: ${backupFile.name}")
        
        if (!backupFile.exists()) {
            println("❌ File does not exist")
            return false
        }
        
        if (backupFile.length() == 0L) {
            println("❌ File is empty")
            return false
        }
        
        java.util.zip.ZipFile(backupFile).use { zipFile ->
            val entries = zipFile.entries().asSequence().toList()
            
            // Check for required files
            val hasMetadata = entries.any { it.name == "metadata.json" }
            val hasDatabase = entries.any { it.name == "renttracker_database.db" }
            
            println("📄 Total entries: ${entries.size}")
            println("📋 Has metadata.json: $hasMetadata")
            println("💾 Has database.db: $hasDatabase")
            
            if (!hasMetadata) {
                println("❌ Missing metadata.json")
                return false
            }
            
            if (!hasDatabase) {
                println("❌ Missing renttracker_database.db")
                return false
            }
            
            // Validate metadata content
            val metadataEntry = entries.find { it.name == "metadata.json" }
            zipFile.getInputStream(metadataEntry).use { input ->
                val metadata = String(input.readBytes())
                println("📝 Metadata content: $metadata")
                
                val isValidJson = metadata.trim().startsWith("{") && metadata.trim().endsWith("}")
                val hasVersion = metadata.contains("version")
                val hasBackupDate = metadata.contains("backupDate")
                
                println("✅ Valid JSON: $isValidJson")
                println("✅ Has version: $hasVersion")
                println("✅ Has backupDate: $hasBackupDate")
                
                if (!isValidJson || !hasVersion || !hasBackupDate) {
                    println("❌ Invalid metadata format")
                    return false
                }
            }
            
            println("✅ Backup file format is valid!")
            return true
        }
    } catch (e: Exception) {
        println("❌ Error validating backup file: ${e.message}")
        e.printStackTrace()
        return false
    }
}

fun main() {
    println("🚀 RentTracker Backup Test Tool")
    println("=" * 50)
    
    // Create test backup
    val testBackup = createTestBackupFile()
    
    // Validate the created backup
    val isValid = validateBackupFile(testBackup)
    
    if (isValid) {
        println("\n🎉 SUCCESS: Test backup file created and validated!")
        println("📱 You can now use this file to test the import functionality")
    } else {
        println("\n❌ FAILURE: Test backup file validation failed")
    }
}

// Helper extension for string repetition
operator fun String.times(n: Int): String = this.repeat(n)
