package com.miassolutions.milkledger.presentation.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditExpensesBinding

class ExpenseEditBottomSheet(
    private val entry: ExpensesEntity,
    private val onSave: (ExpensesEntity) -> Unit,

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

    private fun autoFocusNext() = with(binding) {

        autoSelectOnFocus(etExpenseAmount)
        autoSelectOnFocus(etNotes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ArrayAdapter(
            requireContext(),
            com.miassolutions.milkledger.R.layout.item_expense_type,
            ExpenseType.entries.toTypedArray()
        )


        binding.etExpenseType.setAdapter(adapter)

        binding.etExpenseType.setOnClickListener {
            binding.etExpenseType.showDropDown()
        }

        binding.etExpenseType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = ExpenseType.entries[position]

            Toast.makeText(requireContext(), "$selectedType", Toast.LENGTH_SHORT).show()
        }


        autoFocusNext()


        binding.etExpenseAmount.setText(entry.expenseAmount.toString())
        binding.etNotes.setText(entry.expenseNote ?: "")

        binding.btnSave.setOnClickListener {
            val expenseType = binding.etExpenseType.text?.toString()?.trim()


            val amount = binding.etExpenseAmount.text?.toString()?.toDoubleOrNull()
            val notes = binding.etNotes.text?.toString()?.trim()


            if (!expenseType.isNullOrEmpty() && amount != null) {
                val updatedEntry = entry.copy(
                    expenseTitle = expenseType,
                    expenseAmount = amount,
                    expenseNote = notes
                )
                onSave(updatedEntry)
                dismiss()
            } else {

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
