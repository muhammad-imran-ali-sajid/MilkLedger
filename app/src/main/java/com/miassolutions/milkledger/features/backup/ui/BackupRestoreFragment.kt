package com.miassolutions.milkledger.features.backup.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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
    private var pendingExportBytes: ByteArray? = null
    private var hasLoadedDriveStatusOnce = false

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                showSnackbar("Backup works, but notifications are disabled.")
            }
        }

    private val createLocalBackupLauncher =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
            handleLocalBackupFileCreated(uri)
        }

    private val restoreLocalBackupLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                Toast.makeText(requireContext(), "Restore cancelled", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            showLocalRestoreConfirmation(uri)
        }

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleGoogleSignInResult(result.data)
        }

    override fun setupViews() {
        setupRecyclerView()
        setupClickListeners()
        observeState()
        requestNotificationPermissionIfNeeded()
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

    private fun setupClickListeners() = with(binding) {
        btnDriveLauncher.setOnClickListener {
            val state = viewModel.uiState.value

            if (!state.featureFlagsLoaded) {
                showSnackbar("Checking Google Drive availability. Please wait.")
                return@setOnClickListener
            }

            if (!state.driveBackupFeatureEnabled) {
                showSnackbar("Google Drive backup feature is expired. Contact Developer.")
                return@setOnClickListener
            }

            ensureDrivePermissionThen(PendingDriveAction.CONNECT_ONLY)
        }

        btnDisconnectDrive.setOnClickListener {
            showDisconnectDriveConfirmation()
        }

        btnBackupNow.setOnClickListener {
            val state = viewModel.uiState.value

            if (!state.featureFlagsLoaded) {
                showSnackbar("Checking Google Drive availability. Please wait.")
                return@setOnClickListener
            }

            if (!state.driveBackupFeatureEnabled) {
                showSnackbar("Google Drive backup feature is expired. Contact Developer.")
                return@setOnClickListener
            }

            if (!isDriveConnected()) {
                showSnackbar("Connect Google Drive first.")
                return@setOnClickListener
            }

            viewModel.backupNow()
        }

        btnRefresh.setOnClickListener {
            val state = viewModel.uiState.value

            if (!state.featureFlagsLoaded) {
                showSnackbar("Checking Google Drive availability. Please wait.")
                return@setOnClickListener
            }

            if (!state.driveBackupFeatureEnabled) {
                showSnackbar("Google Drive backup feature is expired. Contact Developer.")
                return@setOnClickListener
            }

            if (!isDriveConnected()) {
                showSnackbar("Connect Google Drive first.")
                return@setOnClickListener
            }

            hasLoadedDriveStatusOnce = false
            viewModel.loadBackupStatus()
        }

        btnCreateLocalBackup.setOnClickListener {
            viewModel.createLocalBackupForExport { fileName, bytes ->
                pendingExportBytes = bytes
                createLocalBackupLauncher.launch(fileName)
            }
        }

        btnRestoreLocalBackup.setOnClickListener {
            restoreLocalBackupLauncher.launch(
                arrayOf(
                    "application/octet-stream",
                    "application/gzip",
                    "application/json",
                    "*/*"
                )
            )
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                    maybeLoadDriveStatus(state)
                }
            }
        }
    }

    private fun maybeLoadDriveStatus(state: BackupStatusUiState) {
        if (hasLoadedDriveStatusOnce) return
        if (!state.featureFlagsLoaded) return
        if (!state.driveBackupFeatureEnabled) return
        if (!isDriveConnected()) return
        if (state.isBusy) return

        hasLoadedDriveStatusOnce = true
        viewModel.loadBackupStatus()
    }

    private fun renderState(state: BackupStatusUiState) {
        val busy = state.isBusy
        val driveConnected = isDriveConnected()
        val featureLoaded = state.featureFlagsLoaded
        val driveFeatureEnabled = state.driveBackupFeatureEnabled
        val canUseDrive = featureLoaded && driveFeatureEnabled

        Log.d(
            "BackupFlags",
            "Fragment render: loaded=$featureLoaded enabled=$driveFeatureEnabled connected=$driveConnected"
        )

        binding.progressBar.isVisible = busy

        binding.btnCreateLocalBackup.isEnabled = !busy
        binding.btnRestoreLocalBackup.isEnabled = !busy

        renderDriveFeatureArea(
            busy = busy,
            featureLoaded = featureLoaded,
            driveFeatureEnabled = driveFeatureEnabled,
            driveConnected = driveConnected
        )

        renderBackupStatus(
            state = state,
            featureLoaded = featureLoaded,
            driveFeatureEnabled = driveFeatureEnabled,
            driveConnected = driveConnected
        )

        val shouldShowDriveBackups =
            canUseDrive && driveConnected && state.driveBackups.isNotEmpty()

        backupAdapter.submitList(
            if (canUseDrive && driveConnected) {
                state.driveBackups
            } else {
                emptyList()
            }
        )

        binding.rvBackups.isVisible = shouldShowDriveBackups

        binding.tvEmptyBackups.isVisible =
            canUseDrive &&
                    driveConnected &&
                    !state.isLoading &&
                    state.driveBackups.isEmpty()

        state.message?.let { message ->
            showSnackbar(message)
            viewModel.clearMessages()
        }

        state.error?.let { error ->
            showSnackbar(error)
            viewModel.clearMessages()
        }
    }

    private fun renderDriveFeatureArea(
        busy: Boolean,
        featureLoaded: Boolean,
        driveFeatureEnabled: Boolean,
        driveConnected: Boolean
    ) = with(binding) {
        when {
            !featureLoaded -> {
                tvDriveDisabledMessage.isVisible = true
                tvDriveDisabledMessage.text = "Checking Google Drive availability..."

                btnDriveLauncher.isVisible = false
                btnDisconnectDrive.isVisible = false
                btnBackupNow.isEnabled = false
                btnRefresh.isEnabled = false
            }

            !driveFeatureEnabled -> {
                tvDriveDisabledMessage.isVisible = true
                tvDriveDisabledMessage.text =
                    "Google Drive backup is temporarily disabled."

                btnDriveLauncher.isVisible = false
                btnDisconnectDrive.isVisible = false
                btnBackupNow.isEnabled = false
                btnRefresh.isEnabled = false
            }

            driveConnected -> {
                tvDriveDisabledMessage.isVisible = false

                btnDriveLauncher.isVisible = false

                btnDisconnectDrive.isVisible = true
                btnDisconnectDrive.isEnabled = !busy

                btnBackupNow.isEnabled = !busy
                btnRefresh.isEnabled = !busy
            }

            else -> {
                tvDriveDisabledMessage.isVisible = false

                btnDriveLauncher.isVisible = true
                btnDriveLauncher.isEnabled = !busy

                btnDisconnectDrive.isVisible = false

                btnBackupNow.isEnabled = false
                btnRefresh.isEnabled = false
            }
        }
    }

    private fun renderBackupStatus(
        state: BackupStatusUiState,
        featureLoaded: Boolean,
        driveFeatureEnabled: Boolean,
        driveConnected: Boolean
    ) = with(binding) {
        when {
            !featureLoaded -> {
                tvBackupStatus.text = "Checking backup availability..."
                tvBackupFile.text = ""
                tvBackupWarning.isVisible = false
            }

            !driveFeatureEnabled -> {
                tvBackupStatus.text = "Google Drive backup disabled"
                tvBackupFile.text = "Local backup is still available."
                tvBackupWarning.isVisible = false
            }

            !driveConnected -> {
                tvBackupStatus.text = "Google Drive not connected"
                tvBackupFile.text = "Connect Google Drive to enable cloud backup."
                tvBackupWarning.isVisible = false
            }

            state.hasSuccessfulBackup -> {
                tvBackupStatus.text =
                    "Last backup: ${formatDateTime(state.lastSuccessfulBackupAt)}"

                tvBackupFile.text =
                    state.lastBackupFileName ?: "Backup file name unavailable"

                tvBackupWarning.isVisible = state.isBackupOld
                tvBackupWarning.text =
                    if (state.isBackupOld) {
                        "Backup is older than 48 hours. Please backup now."
                    } else {
                        ""
                    }
            }

            else -> {
                tvBackupStatus.text = "No successful backup yet"
                tvBackupFile.text = "Create a Google Drive backup to protect data."

                tvBackupWarning.isVisible = true
                tvBackupWarning.text =
                    "No Google Drive backup found. Data is not protected from phone loss."
            }
        }
    }

    private fun ensureDrivePermissionThen(action: PendingDriveAction) {
        if (isDriveConnected()) {
            onDrivePermissionReady(action)
        } else {
            pendingDriveAction = action
            googleSignInLauncher.launch(
                googleDriveAuthManager.getSignInIntent(requireActivity())
            )
        }
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        try {
            val account = GoogleSignIn
                .getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)

            if (account == null) {
                showDriveAuthError("Google account not selected.")
                return
            }

            if (!isDriveConnected()) {
                showDriveAuthError("Google Drive permission was not granted.")
                return
            }

            val action = pendingDriveAction ?: PendingDriveAction.CONNECT_ONLY
            onDrivePermissionReady(action)

        } catch (e: ApiException) {
            Log.e("DriveAuth", "Google sign-in failed. statusCode=${e.statusCode}", e)

            val message = when (e.statusCode) {
                10 -> "Google Sign-In config error. Check SHA-1, package name, and OAuth client."
                12501 -> "Google Drive sign-in cancelled."
                12500 -> "Google Sign-In failed. Check Google Play services or OAuth setup."
                else -> "Google Sign-In failed. Code: ${e.statusCode}"
            }

            showDriveAuthError(message)

        } catch (e: Exception) {
            Log.e("DriveAuth", "Unexpected Google sign-in error", e)
            showDriveAuthError(e.message ?: "Google Sign-In failed.")

        } finally {
            pendingDriveAction = null
        }
    }

    private fun onDrivePermissionReady(action: PendingDriveAction) {
        when (action) {
            PendingDriveAction.CONNECT_ONLY -> {
                showSnackbar("Google Drive connected successfully")
                hasLoadedDriveStatusOnce = false
                viewModel.loadBackupStatus()
            }

            PendingDriveAction.LOAD_STATUS -> {
                viewModel.loadBackupStatus()
            }

            PendingDriveAction.BACKUP_NOW -> {
                viewModel.backupNow()
            }
        }
    }

    private fun showDisconnectDriveConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Disconnect Google Drive?")
            .setMessage(
                """
                Google Drive backup will stop working until you connect again.
                
                Your backup files will not be deleted from Google Drive.
                """.trimIndent()
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Disconnect") { _, _ ->
                revokeDrivePermission()
            }
            .show()
    }

    private fun revokeDrivePermission() {
        googleDriveAuthManager.revokeDriveAccess(
            activity = requireActivity(),
            onSuccess = {
                showSnackbar("Google Drive disconnected")

                hasLoadedDriveStatusOnce = false
                viewModel.clearDriveBackupsAfterDisconnect()
            },
            onError = { exception ->
                Log.e("DriveAuth", "Failed to revoke Drive access", exception)
                showSnackbar(exception?.message ?: "Failed to disconnect Google Drive")
            }
        )
    }

    private fun isDriveConnected(): Boolean {
        return googleDriveAuthManager.hasDrivePermission(requireActivity())
    }

    private fun handleLocalBackupFileCreated(uri: Uri?) {
        if (uri == null) {
            Toast.makeText(requireContext(), "Local backup cancelled", Toast.LENGTH_SHORT).show()
            pendingExportBytes = null
            return
        }

        val bytes = pendingExportBytes
        if (bytes == null) {
            Toast.makeText(requireContext(), "Backup data not available", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val outputStream = requireContext()
                .contentResolver
                .openOutputStream(uri)
                ?: error("Unable to open selected file")

            outputStream.use { output ->
                output.write(bytes)
            }

            pendingExportBytes = null
            showSnackbar("Local backup file saved successfully")

        } catch (e: Exception) {
            pendingExportBytes = null
            showSnackbar(e.message ?: "Failed to save local backup")
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

    private fun showLocalRestoreConfirmation(uri: Uri) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Restore local backup?")
            .setMessage(
                """
                This will replace the current local data with the selected backup file.
                
                Continue only if this backup file is trusted and belongs to this app.
                """.trimIndent()
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Restore") { _, _ ->
                viewModel.restoreLocalBackupFromUri(
                    uri = uri,
                    contentResolver = requireContext().contentResolver,
                    cacheDir = requireContext().cacheDir
                )
            }
            .show()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun showDriveAuthError(message: String) {
        showSnackbar(message)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun formatDateTime(timeMillis: Long): String {
        val formatter = SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        )

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
        CONNECT_ONLY,
        LOAD_STATUS,
        BACKUP_NOW
    }
}