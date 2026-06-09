package com.miassolutions.milkledger.features.backup.drive

data class DriveBackupResult(
    val fileId: String,
    val fileName: String,
    val webViewLink: String?
)