package com.miassolutions.milkledger.zplayground

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.miassolutions.milkledger.core.contstants.Constants.DB_NAME
import com.miassolutions.milkledger.core.managers.BackupManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDriveBackupBinding
import kotlinx.coroutines.launch
import java.io.File

class DriveBackupFragment :
    BaseFragment<FragmentDriveBackupBinding>(FragmentDriveBackupBinding::inflate) {

    private lateinit var backupManager: BackupManager
    private lateinit var backupDir: File
    private lateinit var backupFile: File

    // Launcher for selecting a backup file via SAF (if needed)
    private val restoreFilePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                restoreBackupFromUri(uri)
            } else {
                showToast("No file selected")
            }
        }

    override fun setupViews() {
        // Setup paths
        backupFile = requireContext().getDatabasePath(DB_NAME)
        backupDir = File(requireContext().filesDir, "backups")

        backupManager = BackupManager(requireContext(), backupFile, backupDir)

        // Local backup (no sign-in needed)
        binding.btnSignIn.setOnClickListener {
            showToast("Sign-in not required for local backup")
        }

        // Backup
        binding.btnBackup.setOnClickListener {
            backupLocally()
        }

        // Restore via SAF picker
        binding.btnRestore.setOnClickListener {
            openRestoreFilePicker()
        }

        // Restore from internal backup list
        binding.btnChooseBackup.setOnClickListener {
            showBackupChooser()
        }

        binding.btnDeleteAllBackups.setOnClickListener {
            showDeleteBackupChooser()
        }
    }

    private fun backupLocally() {
        lifecycleScope.launch {
            try {
                val file = backupManager.backupLocally()
                showToast("Backup saved: ${file.name}")
            } catch (e: Exception) {
                showToast("Backup failed: ${e.message}")
            }
        }
    }

    private fun openRestoreFilePicker() {
        // You can restrict to your backup MIME type (e.g., application/octet-stream) if needed
        restoreFilePicker.launch(arrayOf("*/*"))
    }

    private fun restoreBackupFromUri(uri: Uri) {
        lifecycleScope.launch {
            val success = backupManager.restoreFromLocal(uri)
            if (success) {
                showToast("Backup restored successfully")
            } else {
                showToast("Failed to restore backup")
            }
        }
    }




    private fun showDeleteBackupChooser() {
        val backups = getBackupFiles()
        if (backups.isEmpty()) {
            showToast("No backups to delete")
            return
        }

        val names = backups.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Backup")
            .setItems(names) { _, which ->
                val selectedFile = backups[which]
                val deleted = backupManager.deleteBackup(selectedFile)
                showToast(if (deleted) "Deleted: ${selectedFile.name}" else "Failed to delete")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun restoreBackupFromFile(file: File) {
        lifecycleScope.launch {
            try {
                file.inputStream().channel.use { src ->
                    backupFile.outputStream().channel.use { dst ->
                        dst.transferFrom(src, 0, src.size())
                    }
                }
                showToast("Backup restored from file: ${file.name}")
            } catch (e: Exception) {
                showToast("Restore failed: ${e.message}")
            }
        }
    }

    private fun showBackupChooser() {
        val backups = getBackupFiles()
        if (backups.isEmpty()) {
            showToast("No backups found")
            return
        }

        val names = backups.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Choose backup to restore")
            .setItems(names) { _, which ->
                val selectedFile = backups[which]
                restoreBackupFromFile(selectedFile)
            }
            .show()
    }

    private fun getBackupFiles(): List<File> {
        return backupDir.listFiles()
            ?.filter { it.name.startsWith(backupFile.nameWithoutExtension) }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }
}
