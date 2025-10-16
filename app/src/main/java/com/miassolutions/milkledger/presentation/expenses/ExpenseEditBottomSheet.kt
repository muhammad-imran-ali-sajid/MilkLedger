package com.miassolutions.milkledger.presentation.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditExpensesBinding

class ExpenseEditBottomSheet(
    private val entry: ExpensesEntity,
    private val onSave: (ExpensesEntity) -> Unit,
    private val isNewExpense : Boolean = false
) : BottomSheetDialogFragment() {

    private var _binding: BottomsheetEditExpensesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomsheetEditExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isNewExpense) {
            binding.nameLayout.visibility = View.VISIBLE
            binding.tvExpenseTitle.visibility = View.GONE
            binding.etName.setText(entry.expenseTitle)
        } else {
            binding.nameLayout.visibility = View.GONE
            binding.tvExpenseTitle.visibility = View.VISIBLE
            binding.tvExpenseTitle.text = entry.expenseTitle
        }

        binding.etExpenseAmount.setText(entry.expenseAmount.toString())
        binding.etNotes.setText(entry.expenseNote ?: "")

        binding.btnSave.setOnClickListener {
            val name = if (isNewExpense) {
                binding.etName.text?.toString()?.trim()
            } else {
                entry.expenseTitle // Keep original title if not editable
            }

            val amount = binding.etExpenseAmount.text?.toString()?.toDoubleOrNull()
            val notes = binding.etNotes.text?.toString()?.trim()

            if (!name.isNullOrEmpty() && amount != null) {
                val updatedEntry = entry.copy(
                    expenseTitle = name,
                    expenseAmount = amount,
                    expenseNote = notes
                )
                onSave(updatedEntry)
                dismiss()
            } else {
                // Simple validation
                if (isNewExpense && name.isNullOrEmpty()) {
                    binding.nameLayout.error = "Title required"
                } else {
                    binding.nameLayout.error = null
                }

                if (amount == null) {
                    binding.etExpenseAmountLayout.error = "Enter valid amount"
                } else {
                    binding.etExpenseAmountLayout.error = null
                }
            }
        }
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
