package com.miassolutions.milkledger.features.backup.drive

data class DriveBackupFile(
    val fileId: String,
    val name: String,
    val sizeBytes: Long?,
    val createdTimeMillis: Long?,
    val modifiedTimeMillis: Long?,
    val webViewLink: String?
)