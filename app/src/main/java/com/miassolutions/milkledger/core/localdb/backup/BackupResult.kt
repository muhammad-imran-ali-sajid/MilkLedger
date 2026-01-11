package com.miassolutions.milkledger.core.localdb.backup

sealed interface BackupResult {
    object Success : BackupResult
    data class Error(val message: String) : BackupResult
}
