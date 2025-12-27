package com.miassolutions.milkledger.core.extensions

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showDeleteActionDialog(
    title: String = "Caution!!",
    message: String = "Are you sure to delete this record?",
    onDeleteConfirmed: (() -> Unit)?
) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK") { d, _ ->
            if (onDeleteConfirmed != null) {
                onDeleteConfirmed()
            }
            d.dismiss()
        }
        .setNegativeButton("Cancel", null)
        .show()
}