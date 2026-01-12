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
import com.miassolutions.milkledger.debug.DebugDataSeeder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DriveBackupFragment :
    BaseFragment<FragmentDriveBackupBinding>(FragmentDriveBackupBinding::inflate) {

    private val viewModel: BackupRestoreViewModel by viewModels()

    @Inject
    lateinit var debugSeeder: DebugDataSeeder


    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri ?: return@registerForActivityResult

        // Coroutine start ki
        lifecycleScope.launch {
            showLoading(true)

            delay(5000)
            val result = viewModel.backup(uri)

            showLoading(false)
            when (result) {
                is BackupResult.Success ->
                    showSnackbar("Backup successful")

                is BackupResult.Error -> {
                    showSnackbar(result.message)
                }
            }
        }


    }

    private val restoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult


        lifecycleScope.launch {
            showLoading(true)

            val result = viewModel.restore(uri)

            showLoading(false)

            when (result) {
                is BackupResult.Error -> showSnackbar("Restore Failed: ${result.message}")

                BackupResult.Success -> {
                    showLoading(true, "App will restart")
                    showSnackbar("Restore Successful. Restarting app...")
                    // Thora wait taake user message parh sake
                    delay(5000)
                    showLoading(false)
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

        btnSeeder.setOnClickListener {
            lifecycleScope.launch {

                showLoading(true)

                showToast("Seeding Data...")
                debugSeeder.seedDummyData()

                showLoading(false)
                showToast("Data Added")
            }

        }

        btnWipe.setOnClickListener {
            showWipeConfirmation()
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


    private fun showWipeConfirmation() {
        showDialog(
            title = "Wipe All Data?",
            message = "Kya aap waqai tamam data delete karna chahte hain? Is se Accounts aur Transactions sab khatam ho jayega.",
            positiveText = "Yes, Delete Everything",
            onAction = {
                performWipeOperation()
            }
        )
    }

    private fun performWipeOperation() {
        lifecycleScope.launch {
            showLoading(true, "Cleaning Database...")

            // ViewModel function call
            val result = viewModel.clearAllData()

            if (result.isSuccess) {
                showLoading(true, "Data Cleared. Restarting...")
                // Snackbar aksar restart se pehle nazar nahi aata, isliye delay zaroori hai
                delay(2000)

                showLoading(false)
                // Ensure RestartHelper sahi context use kar raha hai
                RestartHelper.restart(requireActivity())
            } else {
                showLoading(false)
                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown Error"
                showSnackbar("Error: $errorMsg")
                // Logcat mein error check karne ke liye:
                android.util.Log.e("WIPE_ERROR", errorMsg)
            }
        }
    }


}
