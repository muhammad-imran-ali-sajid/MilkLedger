package com.miassolutions.milkledger.features.backup.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun scheduleDailyBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val request = PeriodicWorkRequestBuilder<DailyBackupWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .addTag(TAG_DAILY_BACKUP)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_DAILY_BACKUP_WORK,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
    }
    
    fun runBackupNowForTest() {
        Log.d("MilkBackup", "Enqueuing test backup worker...")
        
        val request = OneTimeWorkRequestBuilder<DailyBackupWorker>()
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
            .addTag("milk_ledger_backup_test")
            .build()
        
        WorkManager.getInstance(context).enqueue(request)
        
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(request.id)
            .observeForever { info ->
                Log.d("MilkBackup", "Worker state: ${info?.state}")
                if (info?.outputData != null) {
                    Log.d("MilkBackup", "Worker output: ${info.outputData}")
                }
            }
        
        Log.d("MilkBackup", "Test backup worker enqueued with id: ${request.id}")
    }
    
    companion object {
        const val UNIQUE_DAILY_BACKUP_WORK = "milk_ledger_daily_backup"
        private const val TAG_DAILY_BACKUP = "milk_ledger_backup"
    }
}