package com.miassolutions.milkledger.features.expense.ui.form

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddExpenseBinding
import com.miassolutions.milkledger.databinding.ItemPersonalExpenseBinding
import com.miassolutions.milkledger.features.expense.data.local.ExpenseEntity
import com.miassolutions.milkledger.features.expense.ui.list.ExpenseViewModel
import com.miassolutions.milkledger.utils.extensions.showLedgerDatePicker
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class ExpenseAddFragment :
    BaseFragment<FragmentAddExpenseBinding>(FragmentAddExpenseBinding::inflate) {


    private val viewModel: ExpenseViewModel by viewModels()

    private var selectedDate: LocalDate = LocalDate.now()

    override fun setupViews() {
        binding.etDate.setText(selectedDate.toDisplayFormat())

        binding.btnAddPersonal.setOnClickListener { addPersonalField() }
        binding.btnSave.setOnClickListener { saveNew() }

        setupDatePicker()
    }


    // ─────────────────────────────────────────────────────────────────────────────
    // Add Dynamic Personal Row via ViewBinding (NO findViewById)
    // ─────────────────────────────────────────────────────────────────────────────
    private fun addPersonalField() {
        val itemBinding =
            ItemPersonalExpenseBinding.inflate(layoutInflater, binding.personalContainer, false)

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
        val list = mutableListOf<ExpenseEntity>()

        // Add static expenses
        addStaticExpense("Fuel", binding.etFuelAmount.text.toString(), date, list)
        addStaticExpense("Vehicle", binding.etVehicleAmount.text.toString(), date, list)
        addStaticExpense("Refreshment", binding.etRefreshmentAmount.text.toString(), date, list)

        // Add dynamic personal expenses (static + dynamic saved in list)
        for (i in 0 until binding.personalContainer.childCount) {
            val childView = binding.personalContainer.getChildAt(i)

            // Get binding from child view
            val itemBinding = ItemPersonalExpenseBinding.bind(childView)

            val title = itemBinding.etPersonalTitle.text.toString().trim()
            val amount = itemBinding.etPersonalAmount.text.toString().toDoubleOrNull() ?: continue

//            if (title.isNotEmpty() && amount > 0) {
//                list.add(
//                    ExpensesEntity(
//                        expenseId = UUID.randomUUID().toString(),
//                        date = date,
//                        expenseTitle = title,
//                        expenseAmount = amount,
//                        expenseNote = note,
//                        isDefault = false // dynamic personal
//                    )
//                )
//            }
        }

        if (list.isEmpty()) {
            Toast.makeText(requireContext(), "Enter at least one amount", Toast.LENGTH_SHORT).show()
            return
        }

//        viewModel.saveExpenses(list)
        findNavController().popBackStack()
    }

    private fun addStaticExpense(
        title: String,
        amountStr: String?,
        date: LocalDate,
        list: MutableList<ExpenseEntity>
    ) {
        val amount = amountStr?.toDoubleOrNull() ?: return
//        if (amount > 0) {
//            list.add(
//                ExpensesEntity(
//                    expenseId = UUID.randomUUID().toString(),
//                    date = date,
//                    expenseTitle = title,
//                    expenseAmount = amount,
//
//                    isDefault = true
//                )
//            )
//        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DATE PICKER
    // ─────────────────────────────────────────────────────────────────────────────
    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            showLedgerDatePicker(
                isAuthorized = true,
                initialDate = selectedDate,
                onPicked = { picked ->
                    selectedDate = picked
                    binding.etDate.setText(picked.toDisplayFormat())
                }
            )
        }
    }


}
