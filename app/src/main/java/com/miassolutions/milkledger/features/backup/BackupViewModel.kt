package com.miassolutions.milkledger.features.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localbackup.DatabaseBackupHelper
import com.miassolutions.milkledger.core.localdb.backup.BackupManager
import com.miassolutions.milkledger.core.localdb.backup.BackupResult
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
    private val backupManager: BackupManager
) : ViewModel() {

    private val _status = MutableSharedFlow<String>(replay = 1) // SharedFlow for status/progress
    val status = _status.asSharedFlow()

    suspend fun backup(uri: Uri): BackupResult {

        return backupManager.backupTo(uri)
    }

    suspend fun restore(uri: Uri): BackupResult {
        return backupManager.restoreFrom(uri)
    }


}
