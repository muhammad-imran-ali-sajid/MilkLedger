package com.miassolutions.milkledger.presentation.expenses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditExpensesBinding
import java.time.LocalDate

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

        // Adapter for Expense Types
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_expense_type,
            ExpenseType.entries.toTypedArray()
        )

        binding.etExpenseType.setAdapter(adapter)

        // IMPORTANT — Set the selected entry when editing!
        binding.etExpenseType.setText(entry.expenseTitle, false)

        binding.etExpenseType.setOnClickListener {
            binding.etExpenseType.showDropDown()
        }

        binding.etExpenseType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = ExpenseType.entries[position]
            Toast.makeText(requireContext(), "$selectedType", Toast.LENGTH_SHORT).show()
        }

        autoFocusNext()

        // Set existing values
        binding.etDate.setText(entry.date.toString())
        binding.etExpenseAmount.setText(entry.expenseAmount.toString())

        binding.etNotes.setText(entry.isDefault.toString() ?: "")

        // Save button
        binding.btnSave.setOnClickListener {
            val expenseTypeText = binding.etExpenseType.text?.toString()?.trim()
            val expenseTypeEnum = ExpenseType.entries.find { it.label == expenseTypeText }

            Log.d("ExpenseEditBottomSheet", "${expenseTypeEnum != ExpenseType.PERSONAL}")
            Toast.makeText(requireContext(), "${expenseTypeEnum != ExpenseType.PERSONAL}", Toast.LENGTH_SHORT).show()


            val dateText = binding.etDate.text?.toString()
            val date = LocalDate.parse(dateText)

            val amount = binding.etExpenseAmount.text?.toString()?.toDoubleOrNull()
            val notes = binding.etNotes.text?.toString()?.trim()

            if (expenseTypeEnum != null && amount != null) {

                val updatedEntry = entry.copy(
                    expenseTitle = expenseTypeEnum.label,
                    expenseAmount = amount,
                    date = date,
                    expenseNote = notes,

                    // ⭐ Correct default logic
                    isDefault = true
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

        binding.etDate.setOnClickListener {
            val admin = SharedPrefsHelper.getUserRole(requireContext())
            val isAuth = admin == "admin"
            // Assume you fetch the authorization status dynamically
            val isUserAuthorized = isAuth // Replace with actual auth check

            // Pass the current date as the pre-selected date for better UX
            val initialDate = LocalDate.now()

            showExpenseDatePicker(

                isAuthorized = isUserAuthorized,
                initialDate = initialDate,
                // The selectedDate (LocalDate) is available here!
                onPicked = { selectedDate: LocalDate ->
                    // This is where you pass the result to your ViewModel

                    binding.etDate.setText(selectedDate.toString())
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
