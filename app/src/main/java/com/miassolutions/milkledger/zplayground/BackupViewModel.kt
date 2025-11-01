package com.miassolutions.milkledger.zplayground

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.backup.DatabaseBackupHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val helper: DatabaseBackupHelper
) : ViewModel() {

    private val TAG = "BackupRestoreViewModel"

    private val _status = MutableStateFlow("Idle")
    val status = _status.asStateFlow()


    // Backup to SAF URI
    fun backupDatabaseToUri(uri: Uri) {
        viewModelScope.launch {
            try {
                helper.backupDatabaseToUri(uri, viewModelScope)
                _status.value = "✅ Backup saved successfully"
            } catch (e: Exception) {
                _status.value = "❌ Backup failed: ${e.message}"
            }
        }
    }

    // Existing backup function
    fun backupDatabase() {
        viewModelScope.launch {
            try {
                val file = helper.backupDatabase()
                _status.value = "✅ Backup saved: ${file.absolutePath}"
            } catch (e: Exception) {
                Log.d(TAG, "Backup failed: ❌ Backup failed: ${e.message}")
                _status.value = "❌ Backup failed: ${e.message}"
            }
        }
    }

    // Existing restore function (kept for path-based restores if needed)
    fun restoreDatabase() {
        viewModelScope.launch {
            try {
                val success = helper.restoreDatabase()
                _status.value = if (success) "✅ Restore successful" else "⚠️ Backup file not found"
            } catch (e: Exception) {
                Log.d(TAG, "restoreDatabase: ❌ Restore failed: ${e.message}")
                _status.value = "❌ Restore failed: ${e.message}"
            }
        }
    }

    // New SAF-based restore function
    fun restoreDatabaseFromInputStream(inputStream: InputStream) {
        viewModelScope.launch {
            try {
                val success = helper.restoreDatabase(inputStream)
                _status.value = if (success) "✅ Restore successful" else "⚠️ Failed to restore from file"
            } catch (e: Exception) {
                Log.d(TAG, "restoreDatabaseFromInputStream: ❌ Restore failed: ${e.message}")
                _status.value = "❌ Restore failed: ${e.message}"
            }
        }
    }

    fun getBackupPath() = helper.getBackupPath()
}
