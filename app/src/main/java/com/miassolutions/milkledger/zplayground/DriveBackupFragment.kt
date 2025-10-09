package com.miassolutions.milkledger.zplayground


import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.miassolutions.milkledger.core.managers.BackupManager
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentDriveBackupBinding
import kotlinx.coroutines.launch

class DriveBackupFragment : BaseFragment<FragmentDriveBackupBinding>(FragmentDriveBackupBinding::inflate) {



    private lateinit var backupManager: BackupManager
    private var credential: GoogleAccountCredential? = null

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        showToast("Signed in as ${account.email}")

                        credential = GoogleAccountCredential.usingOAuth2(
                            requireContext(),
                            listOf("https://www.googleapis.com/auth/drive.file")
                        ).apply {
                            selectedAccount = account.account
                        }
                    }
                } catch (e: ApiException) {
                    showToast("Sign-in failed: ${e.message}")
                }
            }
        }

    override fun setupViews() {
        backupManager = BackupManager(requireContext())

        binding.btnSignIn.setOnClickListener {
            signInForDrive()
        }

        binding.btnBackup.setOnClickListener {
            uploadBackup()
        }

        binding.btnRestore.setOnClickListener {
            restoreBackup()
        }
    }

    private fun signInForDrive() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/drive.file"))
            .build()

        val client = GoogleSignIn.getClient(requireContext(), signInOptions)
        signInLauncher.launch(client.signInIntent)
    }

    private fun uploadBackup() {
        val cred = credential
        if (cred == null) {
            showToast("Please sign in first")
            return
        }

        lifecycleScope.launch {
            try {
                backupManager.uploadToDrive(requireActivity() as Activity, cred)
                showToast("Backup uploaded to Drive")
            } catch (e: Exception) {
                showToast("Upload failed: ${e.message}")
            }
        }
    }

    private fun restoreBackup() {
        val cred = credential
        if (cred == null) {
            showToast("Please sign in first")
            return
        }

        lifecycleScope.launch {
            try {
                backupManager.restoreFromDrive(requireActivity() as Activity, cred)
                showToast("Backup restored from Drive")
            } catch (e: Exception) {
                showToast("Restore failed: ${e.message}")
            }
        }
    }
}
