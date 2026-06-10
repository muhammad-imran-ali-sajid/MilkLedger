package com.miassolutions.milkledger.features.backup.data

data class BackupStatusSnapshot(
    val lastDataChangedAt: Long,
    val lastSuccessfulBackupAt: Long,
    val lastBackupFileName: String?,
    val lastBackupError: String?
)