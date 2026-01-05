package com.miassolutions.milkledger.features.expense.ui.form

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.miassolutions.milkledger.core.ui.BaseBottomSheet
import com.miassolutions.milkledger.databinding.BottomSheetEditExpenseBinding
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toPrice
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditExpenseBottomSheet : BaseBottomSheet<BottomSheetEditExpenseBinding>(
    BottomSheetEditExpenseBinding::inflate
) {

    // Args se Expense ID ya Object lein
    private val args: EditExpenseBottomSheetArgs by navArgs()

    // ViewModel jisme updateExpense call ho
    private val viewModel: EditExpenseViewModel by viewModels()

    // Static Categories ki List (Inka Title change nahi ho sakta)
    private val staticCategories = listOf("Fuel", "Vehicle", "Refreshment")


    override fun onViewReady(savedInstanceState: Bundle?) {
        // 1. Expense Load karein (Agar object pass kiya hai to direct use karein)
        val expense = args.expense // Assuming Parcelable pass kiya hai

        setupViews(expense)
        setupClick(expense)
    }

    private fun setupViews(expense: Expense) {
        binding.apply {
            etTitle.setText(expense.title)
            etNote.setText(expense.note)

            // Amount Paisa -> Rupees
            val rupees = expense.amount.toPrice()
            etAmount.setText(rupees) // e.g. "50.50"

            // 🔥 LOGIC: Lock Title for Static Categories
            if (expense.category in staticCategories) {
                etTitle.isEnabled = false
                etTitle.alpha = 0.6f // Thora dhundla dikhaye
                tilTitle.helperText = "Standard category cannot be renamed"
            } else {
                etTitle.isEnabled = true
                tilTitle.helperText = null
            }
        }
    }

    private fun setupClick(originalExpense: Expense) {
        binding.btnUpdate.setOnClickListener {
            val newTitle = binding.etTitle.text.toString()
            val newAmountStr = binding.etAmount.text.toString()
            val newNote = binding.etNote.text.toString()

            if (newAmountStr.isBlank()) {
                binding.etAmount.error = "Required"
                return@setOnClickListener
            }

            // Updated Object banayen
            val updatedExpense = originalExpense.copy(
                title = newTitle,
                amount = newAmountStr.toLongPaisa(), // Rupees -> Paisa
                note = newNote
            )

            // ViewModel ko bhej dein
            viewModel.updateExpense(updatedExpense)
            dismiss()
        }
    }
}