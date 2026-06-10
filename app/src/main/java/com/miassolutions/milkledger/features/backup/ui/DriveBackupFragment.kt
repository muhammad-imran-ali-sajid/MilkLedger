//package com.miassolutions.milkledger.features.backup.ui
//
//import android.app.Activity
//import android.util.Log
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.lifecycleScope
//import com.miassolutions.milkledger.core.localdb.backup.BackupConfig
//import com.miassolutions.milkledger.core.localdb.backup.BackupResult
//import com.miassolutions.milkledger.core.localdb.backup.RestartHelper
//import com.miassolutions.milkledger.core.ui.BaseFragment
//import com.miassolutions.milkledger.databinding.FragmentDriveBackupBinding
//import com.miassolutions.milkledger.features.backup.drive.GoogleDriveAuthManager
//import com.miassolutions.milkledger.features.backup.worker.BackupWorkScheduler
//import dagger.hilt.android.AndroidEntryPoint
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class DriveBackupFragment :
//    BaseFragment<FragmentDriveBackupBinding>(FragmentDriveBackupBinding::inflate) {
//
//    private val viewModel: BackupRestoreViewModel by viewModels()
//
//    @Inject
//    lateinit var backupWorkScheduler: BackupWorkScheduler
//
//    @Inject
//    lateinit var googleDriveAuthManager: GoogleDriveAuthManager
//
//    private val googleSignInLauncher =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                viewModel.uploadBackupToDrive()
//            } else {
//                showToast("Google sign-in cancelled")
//            }
//        }
//
//
//    private val createBackupLauncher = registerForActivityResult(
//        ActivityResultContracts.CreateDocument("application/octet-stream")
//    ) { uri ->
//        uri ?: return@registerForActivityResult
//
//        // Coroutine start ki
//        lifecycleScope.launch {
//            showLoading(true)
//
//            delay(5000)
//            val result = viewModel.backup(uri)
//
//            showLoading(false)
//            when (result) {
//                is BackupResult.Success ->
//                    showSnackbar("Backup successful")
//
//                is BackupResult.Error -> {
//                    showSnackbar(result.message)
//                }
//            }
//        }
//
//
//    }
//
//    private val restoreLauncher = registerForActivityResult(
//        ActivityResultContracts.OpenDocument()
//    ) { uri ->
//        uri ?: return@registerForActivityResult
//
//
//        lifecycleScope.launch {
//            showLoading(true)
//
//            val result = viewModel.restore(uri)
//
//            showLoading(false)
//
//            when (result) {
//                is BackupResult.Error -> showSnackbar("Restore Failed: ${result.message}")
//
//                BackupResult.Success -> {
//                    showLoading(true, "App will restart")
//                    showSnackbar("Restore Successful. Restarting app...")
//                    // Thora wait taake user message parh sake
//                    delay(1000)
//                    showLoading(false)
//                    RestartHelper.restart(requireActivity())
//                }
//            }
//        }
//
//
//    }
//
//
//    override fun setupListeners() = with(binding) {
//        super.setupListeners()
//
//        btnWorker.setOnClickListener {
//
//            backupWorkScheduler.runBackupNowForTest()
//        }
//
//        btnBackup.setOnClickListener {
//            val fileName = "${BackupConfig.BACKUP_PREFIX}${System.currentTimeMillis()}.db"
//            createBackupLauncher.launch(fileName)
//        }
//
//        btnRestore.setOnClickListener {
//            showRestoreConfirmation()
//        }
//
//        btnTest.setOnClickListener {
//            viewModel.testRestoreLatestLocalBackup()
//        }
//
//        btnBackupToDrive.setOnClickListener {
//            if (googleDriveAuthManager.hasDrivePermission(requireActivity())) {
//                viewModel.uploadBackupToDrive()
//            } else {
//                googleSignInLauncher.launch(
//                    googleDriveAuthManager.getSignInIntent(requireActivity())
//                )
//            }
//        }
//
//        btnRestoreFromDrive.setOnClickListener {
//            viewModel.testRestoreLatestDriveBackup()
//        }
//
//
//    }
//
//    private fun showRestoreConfirmation() {
//        showDialog(
//            title = "Restore Backup",
//            message = "Existing data will be overwritten. Continue?",
//            positiveText = "Restore",
//            onAction = {
//                restoreLauncher.launch(
//                    arrayOf("application/octet-stream")
//                )
//            }
//        )
//    }
//
//
//
//    private fun performWipeOperation() {
//        lifecycleScope.launch {
//            showLoading(true, "Cleaning Database...")
//
//            // ViewModel function call
//            val result = viewModel.clearAllData()
//
//            if (result.isSuccess) {
//                showLoading(true, "Data Cleared. Restarting...")
//                // Snackbar aksar restart se pehle nazar nahi aata, isliye delay zaroori hai
//                delay(2000)
//
//                showLoading(false)
//                // Ensure RestartHelper sahi context use kar raha hai
//                RestartHelper.restart(requireActivity())
//            } else {
//                showLoading(false)
//                val errorMsg = result.exceptionOrNull()?.message ?: "Unknown Error"
//                showSnackbar("Error: $errorMsg")
//                // Logcat mein error check karne ke liye:
//                Log.e("WIPE_ERROR", errorMsg)
//            }
//        }
//    }
//
//
//}
