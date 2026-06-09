package com.miassolutions.milkledger.features.backup.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val backupId: String,
    val appName: String = "Milk Ledger",
    val backupFormatVersion: Int = 1,
    val databaseVersion: Int,
    val appVersionName: String,
    val appVersionCode: Long,
    val createdAtMillis: Long,
    val deviceName: String
)