package com.miassolutions.milkledger.presentation.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditExpenseBinding

class ExpenseEditBottomSheet(
    private val entry: ExpensesEntity,
    private val onSave: (ExpensesEntity) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetEditExpenseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun autoFocusNext() = with(binding) {
        autoSelectOnFocus(etExpenseAmount)
        autoSelectOnFocus(etNotes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoFocusNext()

        // Show title only
        binding.nameLayout.visibility = View.GONE
        binding.tvExpenseTitle.visibility = View.VISIBLE
        binding.tvExpenseTitle.text = entry.expenseTitle

        // Populate editable fields
        binding.etExpenseAmount.setText(entry.expenseAmount.toString())
        binding.etNotes.setText(entry.expenseNote ?: "")

        binding.btnSave.setOnClickListener {
            val amount = binding.etExpenseAmount.text?.toString()?.toDoubleOrNull()
            val notes = binding.etNotes.text?.toString()?.trim()

            if (amount == null) {
                binding.etExpenseAmountLayout.error = "Enter valid amount"
                return@setOnClickListener
            } else {
                binding.etExpenseAmountLayout.error = null
            }

            val updated = entry.copy(
                expenseAmount = amount,
                expenseNote = notes
            )

            onSave(updated)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
