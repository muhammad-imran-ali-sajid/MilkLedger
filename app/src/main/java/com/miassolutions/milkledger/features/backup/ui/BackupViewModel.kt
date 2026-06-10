//package com.miassolutions.milkledger.features.backup
//
//import android.net.Uri
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.miassolutions.milkledger.core.localdb.database.AppDatabase
//import com.miassolutions.milkledger.core.localdb.backup.BackupManager
//import com.miassolutions.milkledger.core.localdb.backup.BackupResult
//import com.miassolutions.milkledger.features.backup.data.BackupFileManager
//import com.miassolutions.milkledger.features.backup.data.BackupRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asSharedFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import java.io.File
//import javax.inject.Inject
//
//@HiltViewModel
//class BackupRestoreViewModel @Inject constructor(
//    private val backupManager: BackupManager,
//    private val backupRepository: BackupRepository,
//    private val backupFileManager: BackupFileManager,
//    private val db: AppDatabase
//) : ViewModel() {
//
//    private val _status = MutableSharedFlow<String>(replay = 1) // SharedFlow for status/progress
//    val status = _status.asSharedFlow()
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading = _isLoading.asStateFlow()
//
//    fun testCreateBackup() {
//        viewModelScope.launch {
//            try {
//
//                val backup = backupRepository.createBackupObject()
//
//                Log.d("MilkBackup", "Backup created")
//                Log.d("MilkBackup", "Accounts: ${backup.counts.accounts}")
//                Log.d("MilkBackup", "Milk: ${backup.counts.milkTransactions}")
//                Log.d("MilkBackup", "Ledger: ${backup.counts.ledgerEntries}")
//                Log.d("MilkBackup", "Expenses: ${backup.counts.expenses}")
//                Log.d("MilkBackup", "Notes: ${backup.counts.notes}")
//            } catch (e: Exception) {
//                Log.e("MilkBackup", "Backup creation failed", e)
//            }
//        }
//    }
//
//    fun testCreateLocalBackupFile() {
//        viewModelScope.launch {
//            try {
//                val file = backupRepository.createLocalBackupFile()
//
//                Log.d("MilkBackup", "Local backup file created")
//                Log.d("MilkBackup", "Path: ${file.absolutePath}")
//                Log.d("MilkBackup", "Size: ${file.length()} bytes")
//            } catch (e: Exception) {
//                Log.e("MilkBackup", "Local backup file failed", e)
//            }
//        }
//    }
//
//    // CORRECTED VERSION - Option A: Use BackupFileManager directly
//    fun testRestoreLatestLocalBackup() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            try {
//                // Get the latest backup file using BackupFileManager
//                val latestBackup = backupFileManager.getLatestLocalBackupFile()
//
//                if (latestBackup == null) {
//                    val msg = "No backup files found in milk_ledger_backups directory"
//                    Log.e("MilkBackup", msg)
//                    _status.emit(msg)
//                    return@launch
//                }
//
//                Log.d("MilkBackup", "Restoring from: ${latestBackup.absolutePath}")
//                Log.d("MilkBackup", "File size: ${latestBackup.length()} bytes")
//                Log.d("MilkBackup", "Last modified: ${java.util.Date(latestBackup.lastModified())}")
//
//                // Perform the restore
//                backupRepository.restoreFromLocalBackupFile(latestBackup)
//
//                Log.d("MilkBackup", "Restore completed successfully")
//                _status.emit("Restore completed successfully!")
//
//            } catch (e: Exception) {
//                Log.e("MilkBackup", "Restore failed", e)
//                _status.emit("Restore failed: ${e.message}")
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    fun uploadBackupToDrive() {
//        viewModelScope.launch {
//            try {
//                Log.d("MilkBackup", "Creating and uploading backup to Drive...")
//
//                val result = backupRepository.createAndUploadBackupToDrive()
//
//                Log.d("MilkBackup", "Drive backup uploaded")
//                Log.d("MilkBackup", "File ID: ${result.fileId}")
//                Log.d("MilkBackup", "File Name: ${result.fileName}")
//                Log.d("MilkBackup", "Link: ${result.webViewLink}")
//
//            } catch (e: Exception) {
//                Log.e("MilkBackup", "Drive backup upload failed", e)
//            }
//        }
//    }
//
//
//    fun testListDriveBackups() {
//        viewModelScope.launch {
//            try {
//                val backups = backupRepository.listDriveBackups()
//
//                Log.d("MilkBackup", "Drive backups found: ${backups.size}")
//
//                backups.forEach { backup ->
//                    Log.d(
//                        "MilkBackup",
//                        "Backup: ${backup.name}, size=${backup.sizeBytes}, id=${backup.fileId}"
//                    )
//                }
//            } catch (e: Exception) {
//                Log.e("MilkBackup", "List Drive backups failed", e)
//            }
//        }
//    }
//
//    fun testDownloadLatestDriveBackup() {
//        viewModelScope.launch {
//            try {
//                val backups = backupRepository.listDriveBackups()
//
//                val latest = backups.firstOrNull()
//                if (latest == null) {
//                    Log.e("MilkBackup", "No Drive backup found")
//                    return@launch
//                }
//
//                val downloadedFile = backupRepository.downloadBackupFromDrive(latest)
//
//                Log.d("MilkBackup", "Drive backup downloaded")
//                Log.d("MilkBackup", "Name: ${latest.name}")
//                Log.d("MilkBackup", "Path: ${downloadedFile.absolutePath}")
//                Log.d("MilkBackup", "Size: ${downloadedFile.length()} bytes")
//
//            } catch (e: Exception) {
//                Log.e("MilkBackup", "Download Drive backup failed", e)
//            }
//        }
//    }
//
//
//
//    suspend fun backup(uri: Uri): BackupResult {
//
//        return backupManager.backupTo(uri)
//    }
//
//    suspend fun restore(uri: Uri): BackupResult {
//        return backupManager.restoreFrom(uri)
//    }
//
//
//    // BackupManager.kt mein add karein
//    suspend fun clearAllData(): Result<Unit> = withContext(Dispatchers.IO) {
//        try {
//            // 1. Tables clear karein
//            db.clearAllTables()
//
//            // 2. Auto-increment IDs reset karein
//            db.openHelper.writableDatabase.execSQL("DELETE FROM sqlite_sequence")
//
//            Result.success(Unit)
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
//    }
//
//
//}
