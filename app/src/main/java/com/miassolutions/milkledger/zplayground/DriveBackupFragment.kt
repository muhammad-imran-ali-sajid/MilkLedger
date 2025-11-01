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
import java.io.File

@AndroidEntryPoint
class DriveBackupFragment :
    BaseFragment<FragmentDriveBackupBinding>(FragmentDriveBackupBinding::inflate) {

    private val viewModel: BackupRestoreViewModel by viewModels()


    private val restoreFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                lifecycleScope.launch {
                    try {
                        context?.contentResolver?.openInputStream(uri)?.let { inputStream ->
                            viewModel.restoreDatabaseFromInputStream(inputStream)
                            // DO NOT close the stream here; the ViewModel/Helper will handle it
                        } ?: showToast("Failed to open file")
                    } catch (e: Exception) {
                        showToast("Restore failed: ${e.message}")
                    }
                }
            } else {
                showToast("No file selected")
            }
        }

    // SAF launcher for creating a backup file
    private val createBackupFileLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
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

        // Trigger backup
        binding.btnBackup.setOnClickListener {
            createBackupFileLauncher.launch("milk_ledger_backup.json")
        }

        // Trigger restore via SAF
        binding.btnRestore.setOnClickListener {
            // Launch SAF file picker
            restoreFilePicker.launch(arrayOf("application/json"))
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        // Observe status messages
        viewModel.status.collectState {
            showSnackbar(it)
        }
    }


}
