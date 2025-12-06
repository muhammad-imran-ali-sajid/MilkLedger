package com.miassolutions.milkledger.presentation.expenses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.FragmentAddExpenseBinding
import com.miassolutions.milkledger.databinding.ItemPersonalExpenseBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@AndroidEntryPoint
class ExpenseAddFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!



    private val viewModel: ExpenseViewModel by viewModels()

    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etDate.setText(selectedDate.toDisplayFormat())

        binding.btnAddPersonal.setOnClickListener { addPersonalField() }
        binding.btnSave.setOnClickListener { saveNew() }

        setupDatePicker()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Add Dynamic Personal Row via ViewBinding (NO findViewById)
    // ─────────────────────────────────────────────────────────────────────────────
    private fun addPersonalField() {
        val itemBinding = ItemPersonalExpenseBinding.inflate(layoutInflater, binding.personalContainer, false)

        // Remove this row
        itemBinding.btnRemove.setOnClickListener {
            binding.personalContainer.removeView(itemBinding.root)
        }

        binding.personalContainer.addView(itemBinding.root)
    }



    // ─────────────────────────────────────────────────────────────────────────────
    // SAVE NEW EXPENSES
    // ─────────────────────────────────────────────────────────────────────────────
    private fun saveNew() {
        val date = selectedDate

        val note = binding.etNotes.text?.toString()?.trim()
        val list = mutableListOf<ExpensesEntity>()

        // Add static expenses
        addStaticExpense("Fuel", binding.etFuelAmount.text.toString(), date, list)
        addStaticExpense("Vehicle", binding.etVehicleAmount.text.toString(), date, list)
        addStaticExpense("Refreshment", binding.etRefreshmentAmount.text.toString(), date,  list)

        // Add dynamic personal expenses (static + dynamic saved in list)
        for (i in 0 until binding.personalContainer.childCount) {
            val childView = binding.personalContainer.getChildAt(i)

            // Get binding from child view
            val itemBinding = ItemPersonalExpenseBinding.bind(childView)

            val title = itemBinding.etPersonalTitle.text.toString().trim()
            val amount = itemBinding.etPersonalAmount.text.toString().toDoubleOrNull() ?: continue

            if (title.isNotEmpty() && amount > 0) {
                list.add(
                    ExpensesEntity(
                        expenseId = UUID.randomUUID().toString(),
                        date = date,
                        expenseTitle = title,
                        expenseAmount = amount,
                        expenseNote = note,
                        isDefault = false // dynamic personal
                    )
                )
            }
        }

        if (list.isEmpty()) {
            Toast.makeText(requireContext(), "Enter at least one amount", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.saveExpenses(list)
        findNavController().popBackStack()
    }

    private fun addStaticExpense(
        title: String,
        amountStr: String?,
        date: LocalDate,
        list: MutableList<ExpensesEntity>
    ) {
        val amount = amountStr?.toDoubleOrNull() ?: return
        if (amount > 0) {
            list.add(
                ExpensesEntity(
                    expenseId = UUID.randomUUID().toString(),
                    date = date,
                    expenseTitle = title,
                    expenseAmount = amount,

                    isDefault = true
                )
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DATE PICKER
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            showExpenseDatePicker(
                isAuthorized = true,
                initialDate = selectedDate,
                onPicked = { picked ->
                    selectedDate = picked
                    binding.etDate.setText(picked.toDisplayFormat())
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
