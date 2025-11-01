package com.miassolutions.milkledger.zplayground

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.backup.DatabaseBackupHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val helper: DatabaseBackupHelper
) : ViewModel() {

    private val _status = MutableSharedFlow<String>(replay = 1) // SharedFlow for status/progress
    val status = _status.asSharedFlow()

    /** Backup database to a SAF Uri with progress */
    fun backupDatabaseToUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _status.emit("Backing up...")

                // Pass a lambda for progress updates
                helper.backupDatabaseToUri(uri) { progress ->
                    // Wrap emit in coroutine
                    viewModelScope.launch {
                        _status.emit("Backing up: $progress%")
                    }
                }

                _status.emit("✅ Backup successful")
            } catch (e: Exception) {
                _status.emit("❌ Backup failed: ${e.message}")
            }
        }
    }

    /** Restore database from an InputStream with progress */
    fun restoreDatabaseFromInputStream(inputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _status.emit("Restoring...")

                // Read input stream into bytes to avoid "Stream Closed"
                val bytes = inputStream.readBytes()
                val byteStream = ByteArrayInputStream(bytes)

                helper.restoreDatabase(byteStream) { progress ->
                    viewModelScope.launch {
                        _status.emit("Restoring: $progress%")
                    }
                }

                _status.emit("✅ Restore successful")
            } catch (e: Exception) {
                _status.emit("❌ Restore failed: ${e.message}")
            }
        }
    }


}
