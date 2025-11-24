package com.miassolutions.milkledger.presentation.expenses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditExpensesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class ExpenseEditBottomSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<ExpenseViewModel>()

    companion object {

        private const val ARG_ENTRY = "ARG_ENTRY"

        const val RESULT_KEY = "expense_edit_result"
        const val RESULT_ENTRY = "result_updated_entry"

        fun newInstance(entry: ExpensesEntity): ExpenseEditBottomSheet {
            return ExpenseEditBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ENTRY, entry)
                }
            }
        }
    }

    private var _binding: BottomsheetEditExpensesBinding? = null
    private val binding get() = _binding!!

    private val entry: ExpensesEntity by lazy {
        requireArguments().getParcelable(ARG_ENTRY)!!
    }

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

        setupExpenseTypeAdapter()
        autoFocusNext()
        setExistingValues()
        setupSaveButton()
        setupDatePicker()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Expense type dropdown setup
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupExpenseTypeAdapter() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_expense_type,
            ExpenseType.entries.map { it.label }
        )

        binding.etExpenseType.apply {
            setAdapter(adapter)
            setText(entry.expenseTitle, false)
            setOnClickListener { showDropDown() }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Set existing data into UI fields
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setExistingValues() = with(binding) {
        etDate.setText(entry.date.toString())
        etExpenseAmount.setText(entry.expenseAmount.toString())
        etNotes.setText(entry.expenseNote ?: "")
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Save logic
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {

            val expenseTypeText = binding.etExpenseType.text?.toString()?.trim()
            val expenseTypeEnum = ExpenseType.entries.find {
                it.label == expenseTypeText
            }

            val dateText = binding.etDate.text.toString()
            val date = LocalDate.parse(dateText)

            val amount = binding.etExpenseAmount.text?.toString()?.toDoubleOrNull()
            val notes = binding.etNotes.text?.toString()?.trim()

            if (expenseTypeEnum == null) {
                Toast.makeText(requireContext(), "Select expense type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (amount == null) {
                binding.etExpenseAmountLayout.error = "Enter valid amount"
                return@setOnClickListener
            } else {
                binding.etExpenseAmountLayout.error = null
            }

            Toast.makeText(
                requireContext(),
                "${expenseTypeEnum != ExpenseType.PERSONAL}",
                Toast.LENGTH_SHORT
            ).show()

            val updated = entry.copy(
                expenseTitle = expenseTypeEnum.label,
                expenseAmount = amount,
                date = date,
                expenseNote = notes,
                isDefault = expenseTypeEnum != ExpenseType.PERSONAL
            )

            viewModel.saveExpense(updated)
            Log.d("ExpenseEditBottomSheet", updated.toString())

            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                Bundle().apply { putParcelable(RESULT_ENTRY, updated) }
            )

            dismiss()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Date Picker
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val admin = SharedPrefsHelper.getUserRole(requireContext())
            val isAuthorized = admin == "admin"

            val initialDate = entry.date

            showExpenseDatePicker(
                isAuthorized = isAuthorized,
                initialDate = initialDate,
                onPicked = { selected ->
                    binding.etDate.setText(selected.toString())
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
