package com.miassolutions.milkledger.features.backup.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.backup.drive.DriveBackupFile
import com.miassolutions.milkledger.features.backup.model.BackupStatusUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val backupPrefs: BackupPrefs
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BackupStatusUiState())
    val uiState: StateFlow<BackupStatusUiState> = _uiState.asStateFlow()
    
    fun loadBackupStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                error = null
            )
            
            try {
                val status = backupPrefs.getBackupStatusSnapshot()
                val backups = backupRepository.listDriveBackups()
                
                _uiState.value = BackupStatusUiState(
                    isLoading = false,
                    lastSuccessfulBackupAt = status.lastSuccessfulBackupAt,
                    lastBackupFileName = status.lastBackupFileName,
                    lastBackupError = status.lastBackupError,
                    driveBackups = backups
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load backup status"
                )
            }
        }
    }
    
    fun backupNow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBackupRunning = true,
                message = null,
                error = null
            )
            
            try {
                val result = backupRepository.createAndUploadBackupToDrive()
                val status = backupPrefs.getBackupStatusSnapshot()
                val backups = backupRepository.listDriveBackups()
                
                _uiState.value = _uiState.value.copy(
                    isBackupRunning = false,
                    lastSuccessfulBackupAt = status.lastSuccessfulBackupAt,
                    lastBackupFileName = status.lastBackupFileName,
                    lastBackupError = null,
                    driveBackups = backups,
                    message = "Backup completed successfully"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isBackupRunning = false,
                    error = e.message ?: "Backup failed"
                )
            }
        }
    }
    
    fun restoreBackup(file: DriveBackupFile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRestoreRunning = true,
                message = null,
                error = null
            )
            
            try {
                backupRepository.restoreFromDriveBackup(file)
                
                _uiState.value = _uiState.value.copy(
                    isRestoreRunning = false,
                    message = "Restore completed successfully"
                )
                
                loadBackupStatus()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRestoreRunning = false,
                    error = e.message ?: "Restore failed"
                )
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            message = null,
            error = null
        )
    }
}