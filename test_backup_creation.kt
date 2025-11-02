import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Simple test to create a minimal backup ZIP file
fun createTestBackupFile(): File {
    val testFile = File("test_backup.zip")
    
    ZipOutputStream(FileOutputStream(testFile)).use { zipOut ->
        // Add metadata
        zipOut.putNextEntry(ZipEntry("metadata.json"))
        val metadata = """{"version": 2, "backupDate": ${System.currentTimeMillis()}, "appVersion": "4.8.4"}"""
        zipOut.write(metadata.toByteArray())
        zipOut.closeEntry()
        
        // Add a dummy database file
        zipOut.putNextEntry(ZipEntry("database.db"))
        zipOut.write("dummy database content".toByteArray())
        zipOut.closeEntry()
    }
    
    return testFile
}

// Run this test to create a backup file
fun main() {
    val backupFile = createTestBackupFile()
    println("Created test backup file: ${backupFile.absolutePath}")
    println("File size: ${backupFile.length()} bytes")
}
