package com.renttracker.app.utils

import android.content.Context
import androidx.work.*
import com.renttracker.app.workers.BackupWorker
import java.util.concurrent.TimeUnit

object BackupScheduler {
    
    private const val BACKUP_WORK_NAME = "scheduled_backup"
    
    enum class BackupFrequency(val days: Long) {
        DAILY(1),
        WEEKLY(7),
        DISABLED(0)
    }
    
    fun scheduleBackup(context: Context, frequency: BackupFrequency) {
        val workManager = WorkManager.getInstance(context)
        
        if (frequency == BackupFrequency.DISABLED) {
            cancelScheduledBackup(context)
            return
        }
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            frequency.days,
            TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            BACKUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            backupRequest
        )
    }
    
    fun cancelScheduledBackup(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
    }
    
    fun triggerManualBackup(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueue(backupRequest)
    }
}
