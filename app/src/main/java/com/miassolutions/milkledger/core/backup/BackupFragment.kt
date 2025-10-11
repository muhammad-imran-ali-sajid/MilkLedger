package com.miassolutions.milkledger.core.backup

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentBackupBinding
import kotlinx.coroutines.launch
import java.io.File

/**
 * A reusable fragment UI to handle local backup/restore operations.
 */
class BackupFragment :
    BaseFragment<FragmentBackupBinding>(FragmentBackupBinding::inflate) {

    private lateinit var backupManager: BackupManager
    private lateinit var dbFile: File
    private lateinit var backupDir: File

    private val restorePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { restoreFromUri(it) } ?: showToast("No file selected")
        }

    override fun setupViews() {
        dbFile = requireContext().getDatabasePath("your_database_name.db")
        backupDir = File(requireContext().filesDir, "backups")
        backupManager = BackupManager(requireContext(), dbFile, backupDir)

        binding.apply {
//            btnBackup.setOnClickListener { createBackup() }
//            btnRestorePicker.setOnClickListener { restorePicker.launch(arrayOf("*/*")) }
//            btnRestoreChoose.setOnClickListener { showBackupChooser() }
//            btnDeleteAll.setOnClickListener { confirmDeleteAll() }
        }
    }

    private fun createBackup() = lifecycleScope.launch {
        try {
            val file = backupManager.createLocalBackup()
            showToast("Backup created: ${file.name}")
        } catch (e: Exception) {
            showToast("Backup failed: ${e.message}")
        }
    }

    private fun restoreFromUri(uri: Uri) = lifecycleScope.launch {
        val success = backupManager.restoreFromUri(uri)
        showToast(if (success) "Backup restored. Restarting app..." else "Restore failed")
        if (success) BackupHelper.restartApp(requireContext(), requireActivity())
    }

    private fun restoreFromFile(file: File) = lifecycleScope.launch {
        val success = backupManager.restoreFromFile(file)
        showToast(if (success) "Restored ${file.name}. Restarting..." else "Restore failed")
        if (success) BackupHelper.restartApp(requireContext(), requireActivity())
    }

    private fun showBackupChooser() {
        val backups = backupManager.getAllBackups()
        if (backups.isEmpty()) {
            showToast("No backups found")
            return
        }
        val names = backups.map { it.name }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Select Backup to Restore")
            .setItems(names) { _, which -> restoreFromFile(backups[which]) }
            .show()
    }

    private fun confirmDeleteAll() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Backups?")
            .setMessage("This will permanently delete all backup files.")
            .setPositiveButton("Delete") { _, _ ->
                val success = backupManager.deleteAllBackups()
                showToast(if (success) "All backups deleted" else "Failed to delete all")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}