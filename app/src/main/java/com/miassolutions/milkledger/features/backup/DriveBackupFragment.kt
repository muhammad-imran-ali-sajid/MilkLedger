package com.miassolutions.milkledger.features.backup

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.miassolutions.milkledger.core.localdb.backup.BackupConfig
import com.miassolutions.milkledger.core.localdb.backup.BackupResult
import com.miassolutions.milkledger.core.localdb.backup.RestartHelper
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDriveBackupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DriveBackupFragment :
    BaseFragment<FragmentDriveBackupBinding>(FragmentDriveBackupBinding::inflate) {

    private val viewModel: BackupRestoreViewModel by viewModels()


    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri ?: return@registerForActivityResult

        when (val result = viewModel.backup(uri)) {
            is BackupResult.Success ->
                showSnackbar("Backup successful")

            is BackupResult.Error -> {
                showSnackbar(result.message)
            }
        }
    }

    private val restoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult

        when (val result = viewModel.restore(uri)) {
            is BackupResult.Error -> {
                showSnackbar(result.message)
            }

            BackupResult.Success -> {
                showSnackbar("Restore Successful")

                // Delay thora kam kar dein taake user ko lag na feel ho
                lifecycleScope.launch {
                    delay(300)
                    // Context pass karein
                    RestartHelper.restart(requireActivity())
                }
            }
        }
    }


    override fun setupListeners() = with(binding) {
        super.setupListeners()

        btnBackup.setOnClickListener {
            val fileName = "${BackupConfig.BACKUP_PREFIX}${System.currentTimeMillis()}.db"
            createBackupLauncher.launch(fileName)
        }

        btnRestore.setOnClickListener {
            showRestoreConfirmation()
        }


    }

    private fun showRestoreConfirmation() {
        showDialog(
            title = "Restore Backup",
            message = "Existing data will be overwritten. Continue?",
            positiveText = "Restore",
            onAction = {
                restoreLauncher.launch(
                    arrayOf("application/octet-stream")
                )
            }
        )
    }


    private fun restartApp() {
        val intent = requireActivity().packageManager
            .getLaunchIntentForPackage(requireActivity().packageName)

        intent?.addFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        )

        requireActivity().startActivity(intent)
        requireActivity().finish()
        Runtime.getRuntime().exit(0)
    }


}
