package com.miassolutions.milkledger.features.backup.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentBackupRestoreBinding
import com.miassolutions.milkledger.features.backup.drive.DriveBackupFile
import com.miassolutions.milkledger.features.backup.drive.GoogleDriveAuthManager
import com.miassolutions.milkledger.features.backup.model.BackupStatusUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BackupRestoreFragment :
    BaseFragment<FragmentBackupRestoreBinding>(FragmentBackupRestoreBinding::inflate) {
    
    
    private val viewModel: BackupRestoreViewModel by viewModels()
    
    @Inject
    lateinit var googleDriveAuthManager: GoogleDriveAuthManager
    
    private lateinit var backupAdapter: DriveBackupAdapter
    
    private var pendingDriveAction: PendingDriveAction? = null
    
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                when (pendingDriveAction) {
                    PendingDriveAction.LOAD_STATUS -> viewModel.loadBackupStatus()
                    PendingDriveAction.BACKUP_NOW -> viewModel.backupNow()
                    null -> viewModel.loadBackupStatus()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Google Drive sign-in cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            pendingDriveAction = null
        }
    
    
    override fun setupViews() {
        
        
        setupRecyclerView()
        setupClickListeners()
        observeState()
        
        ensureDrivePermissionThen(PendingDriveAction.LOAD_STATUS)
    }
    
    private fun setupRecyclerView() {
        backupAdapter = DriveBackupAdapter(
            onRestoreClicked = { file ->
                showRestoreConfirmation(file)
            }
        )
        
        binding.rvBackups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = backupAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnBackupNow.setOnClickListener {
            ensureDrivePermissionThen(PendingDriveAction.BACKUP_NOW)
        }
        
        binding.btnRefresh.setOnClickListener {
            ensureDrivePermissionThen(PendingDriveAction.LOAD_STATUS)
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }
    
    private fun renderState(state: BackupStatusUiState) {
        val busy = state.isBusy
        
        binding.progressBar.isVisible = busy
        binding.btnBackupNow.isEnabled = !busy
        binding.btnRefresh.isEnabled = !busy
        
        renderBackupStatus(state)
        
        backupAdapter.submitList(state.driveBackups)
        
//            state.driveBackups.forEach {
//            Log.d("MilkBackup", "Drive file: ${it.name}, size=${it.sizeBytes}")
//        }
        
        binding.rvBackups.isVisible = state.driveBackups.isNotEmpty()
        binding.tvEmptyBackups.isVisible = !state.isLoading && state.driveBackups.isEmpty()
        
        state.message?.let { message ->
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        
        state.error?.let { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    
    private fun renderBackupStatus(state: BackupStatusUiState) {
        if (state.hasSuccessfulBackup) {
            binding.tvBackupStatus.text =
                "Last backup: ${formatDateTime(state.lastSuccessfulBackupAt)}"
            binding.tvBackupFile.text = state.lastBackupFileName ?: "Backup file name unavailable"
        } else {
            binding.tvBackupStatus.text = "No successful backup yet"
            binding.tvBackupFile.text = "Create a Google Drive backup to protect data."
        }
        
        binding.tvBackupWarning.isVisible = state.isBackupOld
        
        binding.tvBackupWarning.text = when {
            !state.hasSuccessfulBackup -> {
                "No Google Drive backup found. Data is not protected from phone loss."
            }
            
            state.isBackupOld -> {
                "Backup is older than 48 hours. Please backup now."
            }
            
            else -> ""
        }
    }
    
    private fun showRestoreConfirmation(file: DriveBackupFile) {
        val dateText = file.modifiedTimeMillis?.let {
            formatDateTime(it)
        } ?: "Unknown date"
        
        val sizeText = file.sizeBytes?.let {
            formatFileSize(it)
        } ?: "Unknown size"
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restore backup?")
            .setMessage(
                """
                This will replace the current local data with this backup.
                
                File:
                ${file.name}
                
                Date:
                $dateText
                
                Size:
                $sizeText
                
                Continue only if you selected the correct backup.
                """.trimIndent()
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Restore") { _, _ ->
                viewModel.restoreBackup(file)
            }
            .show()
    }
    
    private fun ensureDrivePermissionThen(action: PendingDriveAction) {
        if (googleDriveAuthManager.hasDrivePermission(requireActivity())) {
            when (action) {
                PendingDriveAction.LOAD_STATUS -> viewModel.loadBackupStatus()
                PendingDriveAction.BACKUP_NOW -> viewModel.backupNow()
            }
        } else {
            pendingDriveAction = action
            googleSignInLauncher.launch(
                googleDriveAuthManager.getSignInIntent(requireActivity())
            )
        }
    }
    
    private fun formatDateTime(timeMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return formatter.format(Date(timeMillis))
    }
    
    private fun formatFileSize(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        
        return if (mb >= 1) {
            String.format(Locale.getDefault(), "%.2f MB", mb)
        } else {
            String.format(Locale.getDefault(), "%.0f KB", kb)
        }
    }
    
    
    private enum class PendingDriveAction {
        LOAD_STATUS,
        BACKUP_NOW
    }
}