package com.miassolutions.milkledger.features.backup.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.backup.notification.BackupNotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DailyBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val backupRepository: BackupRepository,
    private val backupPrefs: BackupPrefs,
    private val backupNotificationHelper: BackupNotificationHelper
) : CoroutineWorker(appContext, params) {
    
    init {
        Log.d("MilkBackup", "DailyBackupWorker constructed")
    }
    
    override suspend fun doWork(): Result {
        return try {
            Log.d("MilkBackup", "DailyBackupWorker started")
            
            if (!backupPrefs.needsBackup()) {
                Log.d("MilkBackup", "No backup needed. Data unchanged.")
                return Result.success()
            }
            
            Log.d("MilkBackup", "Data changed. Creating Drive backup...")
            
            val result = backupRepository.createAndUploadBackupToDrive()
            
            Log.d("MilkBackup", "Auto backup uploaded: ${result.fileName}")
            
            backupNotificationHelper.showSuccessNotification(result.fileName)
            
            Result.success()
        } catch (e: Exception) {
            Log.e("MilkBackup", "Auto backup failed. attempt=$runAttemptCount", e)
            
            backupPrefs.markBackupFailed(
                e.message ?: "Unknown backup error"
            )
            
            backupNotificationHelper.showFailureNotification(
                "Backup failed. It will retry automatically."
            )
            
            Result.retry()
        }
    }
}