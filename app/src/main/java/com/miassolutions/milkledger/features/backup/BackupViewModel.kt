package com.miassolutions.milkledger.features.backup

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.AppDatabase
import com.miassolutions.milkledger.core.localdb.backup.BackupManager
import com.miassolutions.milkledger.core.localdb.backup.BackupResult
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupManager: BackupManager,
    private val backupRepository: BackupRepository,
    private val db: AppDatabase
) : ViewModel() {

    private val _status = MutableSharedFlow<String>(replay = 1) // SharedFlow for status/progress
    val status = _status.asSharedFlow()
    
    fun testCreateBackup() {
        viewModelScope.launch {
            try {
                val backup = backupRepository.createBackupObject()
                
                Log.d("MilkBackup", "Backup created")
                Log.d("MilkBackup", "Accounts: ${backup.counts.accounts}")
                Log.d("MilkBackup", "Milk: ${backup.counts.milkTransactions}")
                Log.d("MilkBackup", "Ledger: ${backup.counts.ledgerEntries}")
                Log.d("MilkBackup", "Expenses: ${backup.counts.expenses}")
                Log.d("MilkBackup", "Notes: ${backup.counts.notes}")
            } catch (e: Exception) {
                Log.e("MilkBackup", "Backup creation failed", e)
            }
        }
    }

    suspend fun backup(uri: Uri): BackupResult {

        return backupManager.backupTo(uri)
    }

    suspend fun restore(uri: Uri): BackupResult {
        return backupManager.restoreFrom(uri)
    }


    // BackupManager.kt mein add karein
    suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1. Tables clear karein
            db.clearAllTables()

            // 2. Auto-increment IDs reset karein
            db.openHelper.writableDatabase.execSQL("DELETE FROM sqlite_sequence")

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}
