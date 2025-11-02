package com.miassolutions.milkledger.core.notification

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object NotificationPermissionHelper {

    fun hasPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Call this from your Fragment when you want to ensure the permission.
     */
    fun requestPermissionIfNeeded(
        fragment: Fragment,
        launcher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val context = fragment.requireContext()

        when {
            hasPermission(context) -> {
                // Already granted – do nothing
            }

            fragment.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                // Show rationale dialog
                showRationaleDialog(fragment, launcher)
            }

            else -> {
                // Either first time or "Don't ask again" selected previously
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showRationaleDialog(
        fragment: Fragment,
        launcher: ActivityResultLauncher<String>
    ) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Notification Permission Needed")
            .setMessage("This app needs notification permission to alert you about important updates.")
            .setPositiveButton("Allow") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Call this when permission result is denied.
     * If the user selected “Don’t ask again”, show the settings dialog.
     */
    fun handlePermissionDenied(fragment: Fragment) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val context = fragment.requireContext()

        val permanentlyDenied = !fragment.shouldShowRequestPermissionRationale(
            Manifest.permission.POST_NOTIFICATIONS
        )

        if (permanentlyDenied) {
            showSettingsDialog(context)
        }
    }

    private fun showSettingsDialog(context: Context) {
        AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage("You have permanently denied notification permission. Please enable it from app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings(context)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
