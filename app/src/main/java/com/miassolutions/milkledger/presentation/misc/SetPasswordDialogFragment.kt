package com.miassolutions.milkledger.presentation.purchase.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.miassolutions.milkledger.core.managers.PasswordManager
import com.miassolutions.milkledger.databinding.DialogSetPasswordBinding

class SetPasswordDialogFragment(
    private val passwordManager: PasswordManager,
    private val onPasswordSet: () -> Unit
) : DialogFragment() {

    private var _binding: DialogSetPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogSetPasswordBinding.inflate(layoutInflater)

        return AlertDialog.Builder(requireContext())
            .setTitle("Set Password")
            .setView(binding.root)
            .setPositiveButton("Save") { dialog, _ ->
                val pass = binding.etPassword.text.toString()
                val confirm = binding.etConfirmPassword.text.toString()

                when {
                    pass.isBlank() -> {
                        Toast.makeText(requireContext(), "Password cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                    pass != confirm -> {
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        passwordManager.savePassword(pass)
                        Toast.makeText(requireContext(), "Password saved", Toast.LENGTH_SHORT).show()
                        onPasswordSet()
                        dialog.dismiss()
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
