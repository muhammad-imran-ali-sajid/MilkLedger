package com.miassolutions.milkledger.presentation.expenses.ui.detail

import com.miassolutions.milkledger.domain.model.Expense
import java.time.LocalDate
import java.util.UUID

data class ExpenseAddUiState(
    val date: LocalDate = LocalDate.now(),

    // FIXED / BUSINESS EXPENSES
    val businessInputs: List<ExpenseInput> = listOf(
        ExpenseInput(title = "Fuel"),
        ExpenseInput(title = "Vehicle"),
        ExpenseInput(title = "Refreshment")
    ),

    // VARIABLE / PERSONAL EXPENSES
    val personalItems: List<ExpenseInput> = emptyList()
)


data class ExpenseInput(
    val title: String = "",
    val amount: Double = 0.0,
    val note: String? = null
) {

    fun isValid(): Boolean =
        title.isNotBlank() && amount > 0.0

    fun toExpense(
        date: LocalDate,
        isBusiness: Boolean
    ): Expense =
        Expense(
            id = UUID.randomUUID().toString(),
            date = date,
            title = title.trim(),
            amount = amount,
            note = note,
            isBusiness = isBusiness
        )
}


sealed interface ExpenseUiEvent {
    data class ShowMessage(val message: String) : ExpenseUiEvent
    data object Dismiss : ExpenseUiEvent
}
