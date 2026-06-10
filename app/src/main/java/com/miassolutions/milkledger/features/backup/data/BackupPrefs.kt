package com.miassolutions.milkledger.features.backup.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val prefs = context.getSharedPreferences(
        "milk_ledger_backup_prefs",
        Context.MODE_PRIVATE
    )
    
    fun markDataChanged(timeMillis: Long = System.currentTimeMillis()) {
        prefs.edit {
            putLong(KEY_LAST_DATA_CHANGED_AT, timeMillis)
        }
    }
    
    fun markBackupSuccess(
        timeMillis: Long = System.currentTimeMillis(),
        fileName: String? = null
    ) {
        prefs.edit {
            putLong(KEY_LAST_SUCCESSFUL_BACKUP_AT, timeMillis)
            putString(KEY_LAST_BACKUP_FILE_NAME, fileName)
            putString(KEY_LAST_BACKUP_ERROR, null)
        }
    }
    
    fun markBackupFailed(error: String) {
        prefs.edit {
            putString(KEY_LAST_BACKUP_ERROR, error)
        }
    }
    
    fun getLastDataChangedAt(): Long {
        return prefs.getLong(KEY_LAST_DATA_CHANGED_AT, 0L)
    }
    
    fun getLastSuccessfulBackupAt(): Long {
        return prefs.getLong(KEY_LAST_SUCCESSFUL_BACKUP_AT, 0L)
    }
    
    fun getLastBackupFileName(): String? {
        return prefs.getString(KEY_LAST_BACKUP_FILE_NAME, null)
    }
    
    fun getLastBackupError(): String? {
        return prefs.getString(KEY_LAST_BACKUP_ERROR, null)
    }
    
    fun needsBackup(): Boolean {
        return getLastDataChangedAt() > getLastSuccessfulBackupAt()
    }
    
    fun getBackupStatusSnapshot(): BackupStatusSnapshot {
        return BackupStatusSnapshot(
            lastDataChangedAt = getLastDataChangedAt(),
            lastSuccessfulBackupAt = getLastSuccessfulBackupAt(),
            lastBackupFileName = getLastBackupFileName(),
            lastBackupError = getLastBackupError()
        )
    }
    
    companion object {
        private const val KEY_LAST_DATA_CHANGED_AT = "last_data_changed_at"
        private const val KEY_LAST_SUCCESSFUL_BACKUP_AT = "last_successful_backup_at"
        private const val KEY_LAST_BACKUP_FILE_NAME = "last_backup_file_name"
        private const val KEY_LAST_BACKUP_ERROR = "last_backup_error"
    }
}