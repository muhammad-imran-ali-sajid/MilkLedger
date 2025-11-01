package com.miassolutions.milkledger.zplayground

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.miassolutions.milkledger.core.managers.BackupManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.AppDatabase
import com.miassolutions.milkledger.databinding.FragmentDriveBackupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class DriveBackupFragment :
    BaseFragment<FragmentDriveBackupBinding>(FragmentDriveBackupBinding::inflate) {

    private val viewModel: BackupRestoreViewModel by viewModels()

    // Restore SAF launcher
    private val restoreFilePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            lifecycleScope.launch {
                try {
                    context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
                        // Pass a buffered copy to avoid Stream Closed
                        val bytes = inputStream.readBytes()
                        viewModel.restoreDatabaseFromInputStream(ByteArrayInputStream(bytes))
                    }
                } catch (e: Exception) {
                    showToast("Restore failed: ${e.message}")
                }
            }
        } else showToast("No file selected")
    }

    // Backup SAF launcher
    private val createBackupFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            lifecycleScope.launch {
                try {
                    viewModel.backupDatabaseToUri(uri)
                } catch (e: Exception) {
                    showToast("Backup failed: ${e.message}")
                }
            }
        } else showToast("Backup cancelled")
    }

    override fun setupViews() {
        binding.btnBackup.setOnClickListener {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            createBackupFileLauncher.launch("milk_ledger_backup_$timestamp.json")
        }

        binding.btnRestore.setOnClickListener {
            restoreFilePicker.launch(arrayOf("application/json"))
        }

        viewModel.status.collectState { message ->
            // Show progress or final message
            showSnackbar(message)
        }
    }
}
