package com.renttracker.app.data.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Utility class for managing file storage operations
 */
class FileStorageManager(private val context: Context) {
    
    private val documentsDir: File by lazy {
        val dir = File(context.filesDir, "documents")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }

    /**
     * Saves a file from URI to app-specific storage
     * @param sourceUri The URI of the file to save
     * @param fileName The name to save the file as
     * @return The absolute path of the saved file, or null if failed
     */
    fun saveFile(sourceUri: Uri, fileName: String): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            inputStream?.use { input ->
                val outputFile = File(documentsDir, fileName)
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
                outputFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Deletes a file from storage
     * @param filePath The absolute path of the file to delete
     * @return True if deletion was successful, false otherwise
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Checks if a file exists
     * @param filePath The absolute path of the file
     * @return True if file exists, false otherwise
     */
    fun fileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the size of a file in bytes
     * @param filePath The absolute path of the file
     * @return Size in bytes, or 0 if file doesn't exist
     */
    fun getFileSize(filePath: String): Long {
        return try {
            val file = File(filePath)
            if (file.exists()) file.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Gets the MIME type from a URI
     * @param uri The URI to get MIME type from
     * @return MIME type string, or null if not found
     */
    fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    /**
     * Gets file extension from filename
     * @param fileName The filename
     * @return File extension without dot, or empty string if no extension
     */
    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    /**
     * Gets file extension from MIME type
     * @param mimeType The MIME type
     * @return File extension without dot, or empty string if not found
     */
    fun getExtensionFromMimeType(mimeType: String): String {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: ""
    }

    /**
     * Generates a unique filename to avoid conflicts
     * @param originalName The original filename
     * @return A unique filename with timestamp
     */
    fun generateUniqueFileName(originalName: String): String {
        val timestamp = System.currentTimeMillis()
        val extension = getFileExtension(originalName)
        val nameWithoutExt = originalName.substringBeforeLast('.', originalName)
        return "${nameWithoutExt}_${timestamp}.${extension}"
    }

    /**
     * Gets the total storage used by documents in bytes
     * @return Total size in bytes
     */
    fun getTotalStorageUsed(): Long {
        return try {
            documentsDir.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Formats file size for display
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }
}
