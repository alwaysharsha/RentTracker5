package com.renttracker.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.renttracker.app.utils.GoogleDriveBackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val backupManager = GoogleDriveBackupManager(applicationContext)
            
            // Check if signed in
            if (!backupManager.isSignedIn()) {
                return@withContext Result.failure()
            }
            
            // Get signed in account and initialize drive service
            val account = backupManager.getSignedInAccount()
            if (account == null) {
                return@withContext Result.failure()
            }
            
            backupManager.initializeDriveService(account)
            
            // Get database path
            val databasePath = applicationContext.getDatabasePath("rent_tracker.db").absolutePath
            
            // Perform backup
            val result = backupManager.backupDatabase(databasePath)
            
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
