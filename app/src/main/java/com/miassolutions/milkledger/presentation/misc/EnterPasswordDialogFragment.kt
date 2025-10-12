package com.miassolutions.milkledger.presentation.misc

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.miassolutions.milkledger.core.managers.PasswordManager
import com.miassolutions.milkledger.databinding.DialogEnterPasswordBinding

class EnterPasswordDialogFragment(
    private val passwordManager: PasswordManager,
    private val onSuccess: () -> Unit,
    private val onForgotPassword: () -> Unit
) : DialogFragment() {

    private var _binding: DialogEnterPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEnterPasswordBinding.inflate(layoutInflater)

        binding.tvForgotPassword.setOnClickListener {
            dismiss()
            onForgotPassword()
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Enter Password")
            .setView(binding.root)
            .setPositiveButton("OK") { dialog, _ ->
                val input = binding.etPassword.text.toString()
                if (passwordManager.isPasswordCorrect(input)) {
                    onSuccess()
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Incorrect password", Toast.LENGTH_SHORT).show()
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
