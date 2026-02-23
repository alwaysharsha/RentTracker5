package com.renttracker.app.utils

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.http.FileContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Collections

class GoogleDriveBackupManager(private val context: Context) {
    
    companion object {
        private const val BACKUP_FOLDER_NAME = "RentTracker_Backups"
        private const val BACKUP_FILE_NAME = "renttracker_backup.db"
    }
    
    private var driveService: Drive? = null
    
    private fun getSignInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
    }
    
    fun getSignInClient(): GoogleSignInClient {
        return GoogleSignIn.getClient(context, getSignInOptions())
    }
    
    fun getSignInIntent(): Intent {
        return getSignInClient().signInIntent
    }
    
    suspend fun trySilentSignIn(): GoogleSignInAccount? = withContext(Dispatchers.IO) {
        try {
            val client = getSignInClient()
            val task = client.silentSignIn()
            if (task.isSuccessful) {
                task.result
            } else {
                // Task is not complete, would need user interaction
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("GoogleDriveBackupManager", "Silent sign-in failed", e)
            null
        }
    }
    
    fun initializeDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singleton(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        
        driveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("RentTracker")
            .build()
    }
    
    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && driveService != null
    }
    
    fun getSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
    
    suspend fun signOut() = withContext(Dispatchers.IO) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        
        val client = GoogleSignIn.getClient(context, signInOptions)
        client.signOut()
        driveService = null
    }
    
    suspend fun backupDatabase(databasePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(
                Exception("Not signed in to Google Drive")
            )
            
            // Get or create backup folder
            val folderId = getOrCreateBackupFolder(service)
            
            // Check if backup file already exists
            val existingFileId = findBackupFile(service, folderId)
            
            // Read database file
            val dbFile = java.io.File(databasePath)
            if (!dbFile.exists()) {
                return@withContext Result.failure(Exception("Database file not found"))
            }
            
            val fileMetadata = File().apply {
                name = BACKUP_FILE_NAME
                parents = listOf(folderId)
            }
            
            val mediaContent = FileContent(
                "application/x-sqlite3",
                dbFile
            )
            
            val uploadedFile = if (existingFileId != null) {
                // Update existing file
                service.files().update(existingFileId, fileMetadata, mediaContent)
                    .execute()
            } else {
                // Create new file
                service.files().create(fileMetadata, mediaContent)
                    .setFields("id, name, modifiedTime")
                    .execute()
            }
            
            Result.success("Backup successful: ${uploadedFile.name}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restoreDatabase(databasePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.failure(
                Exception("Not signed in to Google Drive")
            )
            
            // Find backup folder
            val folderId = findBackupFolder(service) ?: return@withContext Result.failure(
                Exception("No backup found on Google Drive")
            )
            
            // Find backup file
            val fileId = findBackupFile(service, folderId) ?: return@withContext Result.failure(
                Exception("No backup file found")
            )
            
            // Download file
            val outputStream = ByteArrayOutputStream()
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            
            // Write to database file
            val dbFile = java.io.File(databasePath)
            FileOutputStream(dbFile).use { fos ->
                fos.write(outputStream.toByteArray())
            }
            
            Result.success("Database restored successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLastBackupTime(): Result<Long?> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: return@withContext Result.success(null)
            
            val folderId = findBackupFolder(service) ?: return@withContext Result.success(null)
            val fileId = findBackupFile(service, folderId) ?: return@withContext Result.success(null)
            
            val file = service.files().get(fileId)
                .setFields("modifiedTime")
                .execute()
            
            Result.success(file.modifiedTime?.value)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun getOrCreateBackupFolder(service: Drive): String {
        val folderId = findBackupFolder(service)
        if (folderId != null) {
            return folderId
        }
        
        // Create folder
        val folderMetadata = File().apply {
            name = BACKUP_FOLDER_NAME
            mimeType = "application/vnd.google-apps.folder"
        }
        
        val folder = service.files().create(folderMetadata)
            .setFields("id")
            .execute()
        
        return folder.id
    }
    
    private fun findBackupFolder(service: Drive): String? {
        val result = service.files().list()
            .setQ("name='$BACKUP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
        
        return result.files.firstOrNull()?.id
    }
    
    private fun findBackupFile(service: Drive, folderId: String): String? {
        val result = service.files().list()
            .setQ("name='$BACKUP_FILE_NAME' and '$folderId' in parents and trashed=false")
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()
        
        return result.files.firstOrNull()?.id
    }
}
