package com.miassolutions.milkledger.features.backup.model

import com.miassolutions.milkledger.features.backup.drive.DriveBackupFile

data class BackupStatusUiState(
    val isLoading: Boolean = false,
    val isBackupRunning: Boolean = false,
    val isRestoreRunning: Boolean = false,
    
    val lastSuccessfulBackupAt: Long = 0L,
    val lastBackupFileName: String? = null,
    val lastBackupError: String? = null,
    
    val driveBackups: List<DriveBackupFile> = emptyList(),
    
    val message: String? = null,
    val error: String? = null
) {
    val hasSuccessfulBackup: Boolean
        get() = lastSuccessfulBackupAt > 0L
    
    val isBackupOld: Boolean
        get() {
            if (lastSuccessfulBackupAt <= 0L) return true
            
            val now = System.currentTimeMillis()
            val fortyEightHoursMillis = 48L * 60L * 60L * 1000L
            
            return now - lastSuccessfulBackupAt > fortyEightHoursMillis
        }
    
    val isBusy: Boolean
        get() = isLoading || isBackupRunning || isRestoreRunning
}