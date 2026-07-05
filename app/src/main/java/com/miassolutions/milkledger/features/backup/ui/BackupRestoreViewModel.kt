package com.miassolutions.milkledger.features.backup.ui

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.backup.data.BackupRepository
import com.miassolutions.milkledger.features.backup.drive.DriveBackupFile
import com.miassolutions.milkledger.features.backup.model.BackupStatusUiState
import com.miassolutions.milkledger.features.remoteconfig.domain.FeatureFlagsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class BackupRestoreViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val backupPrefs: BackupPrefs,
    private val featureFlagsRepository: FeatureFlagsRepository
) : ViewModel() {

    private val _backupState = MutableStateFlow(BackupStatusUiState())

    val uiState: StateFlow<BackupStatusUiState> =
        combine(
            _backupState,
            featureFlagsRepository.featureFlags
        ) { backupState, flags ->

            Log.d(
                "BackupFlags",
                "Combining backup state with flags: $flags"
            )

            backupState.copy(
                featureFlagsLoaded = flags.loaded,
                driveBackupFeatureEnabled = flags.driveBackupEnabled
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BackupStatusUiState()
        )

    init {
        refreshFeatureFlags()
    }

    private fun refreshFeatureFlags() {
        viewModelScope.launch {
            featureFlagsRepository.refresh()
        }
    }

    fun createLocalBackupForExport(
        onReady: (fileName: String, bytes: ByteArray) -> Unit
    ) {
        viewModelScope.launch {
            _backupState.update {
                it.copy(
                    isBackupRunning = true,
                    message = null,
                    error = null
                )
            }

            try {
                val bytes = backupRepository.createLocalBackupBytes()
                val fileName = backupRepository.generateBackupFileNameForExport()

                _backupState.update {
                    it.copy(
                        isBackupRunning = false,
                        message = "Local backup created"
                    )
                }

                onReady(fileName, bytes)

            } catch (e: Exception) {
                _backupState.update {
                    it.copy(
                        isBackupRunning = false,
                        error = e.message ?: "Failed to create local backup"
                    )
                }
            }
        }
    }

    fun restoreLocalBackupFromUri(
        uri: Uri,
        contentResolver: ContentResolver,
        cacheDir: File
    ) {
        viewModelScope.launch {
            _backupState.update {
                it.copy(
                    isRestoreRunning = true,
                    message = null,
                    error = null
                )
            }

            try {
                val restoredFile = backupRepository.copyUriToTempBackupFile(
                    uri = uri,
                    contentResolver = contentResolver,
                    cacheDir = cacheDir
                )

                backupRepository.restoreFromLocalBackupFile(restoredFile)

                _backupState.update {
                    it.copy(
                        isRestoreRunning = false,
                        message = "Local backup restored successfully"
                    )
                }

                if (featureFlagsRepository.isDriveBackupEnabledNow()) {
                    loadBackupStatus()
                }

            } catch (e: Exception) {
                _backupState.update {
                    it.copy(
                        isRestoreRunning = false,
                        error = e.message ?: "Local restore failed"
                    )
                }
            }
        }
    }

    fun loadBackupStatus() {
        if (!featureFlagsRepository.isDriveBackupEnabledNow()) {
            _backupState.update {
                it.copy(
                    isLoading = false,
                    error = "Google Drive backup feature is expired. Contact Developer."
                )
            }
            return
        }

        viewModelScope.launch {
            _backupState.update {
                it.copy(
                    isLoading = true,
                    message = null,
                    error = null
                )
            }

            try {
                val status = backupPrefs.getBackupStatusSnapshot()
                val backups = backupRepository.listDriveBackups()

                _backupState.update {
                    it.copy(
                        isLoading = false,
                        lastSuccessfulBackupAt = status.lastSuccessfulBackupAt,
                        lastBackupFileName = status.lastBackupFileName,
                        lastBackupError = status.lastBackupError,
                        driveBackups = backups
                    )
                }

            } catch (e: Exception) {
                _backupState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load backup status"
                    )
                }
            }
        }
    }

    fun backupNow() {
        if (!featureFlagsRepository.isDriveBackupEnabledNow()) {
            _backupState.update {
                it.copy(
                    isBackupRunning = false,
                    error = "Google Drive backup feature is expired. Contact Developer."
                )
            }
            return
        }

        viewModelScope.launch {
            _backupState.update {
                it.copy(
                    isBackupRunning = true,
                    message = null,
                    error = null
                )
            }

            try {
                backupRepository.createAndUploadBackupToDrive()

                val status = backupPrefs.getBackupStatusSnapshot()
                val backups = backupRepository.listDriveBackups()

                _backupState.update {
                    it.copy(
                        isBackupRunning = false,
                        lastSuccessfulBackupAt = status.lastSuccessfulBackupAt,
                        lastBackupFileName = status.lastBackupFileName,
                        lastBackupError = null,
                        driveBackups = backups,
                        message = "Backup completed successfully"
                    )
                }

            } catch (e: Exception) {
                _backupState.update {
                    it.copy(
                        isBackupRunning = false,
                        error = e.message ?: "Backup failed"
                    )
                }
            }
        }
    }

    fun restoreBackup(file: DriveBackupFile) {
        if (!featureFlagsRepository.isDriveBackupEnabledNow()) {
            _backupState.update {
                it.copy(
                    isRestoreRunning = false,
                    error = "Google Drive restore is disabled right now."
                )
            }
            return
        }

        viewModelScope.launch {
            _backupState.update {
                it.copy(
                    isRestoreRunning = true,
                    message = null,
                    error = null
                )
            }

            try {
                backupRepository.restoreFromDriveBackup(file)

                _backupState.update {
                    it.copy(
                        isRestoreRunning = false,
                        message = "Restore completed successfully"
                    )
                }

                loadBackupStatus()

            } catch (e: Exception) {
                _backupState.update {
                    it.copy(
                        isRestoreRunning = false,
                        error = e.message ?: "Restore failed"
                    )
                }
            }
        }
    }

    fun clearDriveBackupsAfterDisconnect() {
        _backupState.update {
            it.copy(
                driveBackups = emptyList(),
                lastBackupFileName = null,
                lastSuccessfulBackupAt = 0L,
                lastBackupError = null
            )
        }
    }

    fun clearMessages() {
        _backupState.update {
            it.copy(
                message = null,
                error = null
            )
        }
    }
}