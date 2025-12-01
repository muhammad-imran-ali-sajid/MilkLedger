package com.miassolutions.milkledger.presentation.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.miassolutions.milkledger.core.prefs.SharedPrefsHelper
import com.miassolutions.milkledger.core.util.autoSelectOnFocus
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.BottomsheetEditExpensesBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@AndroidEntryPoint
class ExpenseEditBottomSheet : BottomSheetDialogFragment() {

    private val viewModel by viewModels<ExpenseViewModel>()

    companion object {
        private const val ARG_ENTRY = "ARG_ENTRY"

        fun newAddInstance(): ExpenseEditBottomSheet = ExpenseEditBottomSheet()

        fun newEditInstance(entry: ExpensesEntity): ExpenseEditBottomSheet {
            return ExpenseEditBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ENTRY, entry)
                }
            }
        }
    }

    private var _binding: BottomsheetEditExpensesBinding? = null
    private val binding get() = _binding!!

    private val isEditMode: Boolean
        get() = arguments?.containsKey(ARG_ENTRY) == true

    private val editEntry: ExpensesEntity?
        get() = arguments?.getParcelable(ARG_ENTRY)

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
        autoFocusNext()
        setupUI()
        setupDatePicker()
        setupSaveButton()
    }

    private fun autoFocusNext() = with(binding) {
        autoSelectOnFocus(etFuelAmount)
        autoSelectOnFocus(etVehicleAmount)
        autoSelectOnFocus(etRefreshmentAmount)
        autoSelectOnFocus(etPersonalAmount)
        autoSelectOnFocus(etNotes)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Date safe parser (avoids crash)
    // ─────────────────────────────────────────────────────────────────────────────
    private fun getSelectedDate(): LocalDate? {
        val text = binding.etDate.text?.toString()?.trim()
        return try {
            LocalDate.parse(text)
        } catch (e: Exception) {
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // UI setup based on ADD / EDIT mode
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupUI() {
        if (isEditMode) {
            val entry = editEntry!!

            binding.etDate.setText(entry.date.toString())
            binding.etNotes.setText(entry.expenseNote ?: "")

            when (entry.expenseTitle) {
                "Fuel" -> binding.etFuelAmount.setText(entry.expenseAmount.toString())
                "Vehicle" -> binding.etVehicleAmount.setText(entry.expenseAmount.toString())
                "Refreshment" -> binding.etRefreshmentAmount.setText(entry.expenseAmount.toString())
                "Personal" -> binding.etPersonalAmount.setText(entry.expenseAmount.toString())
            }
        } else {
            // ADD MODE → always set today's date (fixes crash)
            binding.etDate.setText(LocalDate.now().toString())
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SAVE BUTTON
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (isEditMode) saveEditedEntry()
            else saveNewEntries()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ADD MODE → Create multiple entries
    // ─────────────────────────────────────────────────────────────────────────────
    private fun saveNewEntries() {

        val date = getSelectedDate()
        if (date == null) {
            Toast.makeText(requireContext(), "Please select valid date", Toast.LENGTH_SHORT).show()
            return
        }

        val note = binding.etNotes.text?.toString()?.trim()

        val fuel = binding.etFuelAmount.text.toString().toDoubleOrNull() ?: 0.0
        val vehicle = binding.etVehicleAmount.text.toString().toDoubleOrNull() ?: 0.0
        val refreshment = binding.etRefreshmentAmount.text.toString().toDoubleOrNull() ?: 0.0
        val personal = binding.etPersonalAmount.text.toString().toDoubleOrNull() ?: 0.0

        val list = mutableListOf<ExpensesEntity>()

        fun add(title: String, amount: Double, default: Boolean) {
            if (amount > 0) {
                list.add(
                    ExpensesEntity(
                        expenseId = UUID.randomUUID().toString(),
                        date = date,
                        expenseTitle = title,
                        expenseAmount = amount,
                        expenseNote = note,
                        isDefault = default
                    )
                )
            }
        }

        add("Fuel", fuel, true)
        add("Vehicle", vehicle, true)
        add("Refreshment", refreshment, true)
        add("Personal", personal, false)

        if (list.isEmpty()) {
            Toast.makeText(requireContext(), "Enter at least one amount", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveExpenses(list)
        dismiss()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // EDIT MODE → Update ONLY selected entry
    // ─────────────────────────────────────────────────────────────────────────────
    private fun saveEditedEntry() {
        val old = editEntry ?: return

        val date = getSelectedDate()
        if (date == null) {
            Toast.makeText(requireContext(), "Please select valid date", Toast.LENGTH_SHORT).show()
            return
        }

        val note = binding.etNotes.text?.toString()?.trim()

        val newAmount = when (old.expenseTitle) {
            "Fuel" -> binding.etFuelAmount.text.toString().toDoubleOrNull()
            "Vehicle" -> binding.etVehicleAmount.text.toString().toDoubleOrNull()
            "Refreshment" -> binding.etRefreshmentAmount.text.toString().toDoubleOrNull()
            "Personal" -> binding.etPersonalAmount.text.toString().toDoubleOrNull()
            else -> null
        }

        if (newAmount == null || newAmount <= 0) {
            Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val updated = old.copy(
            date = date,
            expenseAmount = newAmount,
            expenseNote = note,
            updatedAt = LocalDateTime.now().toString()
        )

        viewModel.saveExpense(updated)
        dismiss()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DATE PICKER
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {

            val role = SharedPrefsHelper.getUserRole(requireContext())
            val isAuthorized = role == "admin"

            val initialDate = getSelectedDate() ?: LocalDate.now()

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
