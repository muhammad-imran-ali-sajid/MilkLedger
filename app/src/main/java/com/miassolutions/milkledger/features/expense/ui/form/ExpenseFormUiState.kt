package com.miassolutions.milkledger.features.expense.ui.form

import java.time.LocalDate
import java.util.UUID

data class PersonalExpenseUi(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val amount: String = "",
    val titleError: String? = null,
    val amountError: String? = null
)

data class ExpenseFormUiState(
    val date: LocalDate = LocalDate.now(),
    val fuelAmount: String = "",
    val vehicleAmount: String = "",
    val refreshmentAmount: String = "",
    val personalExpenses: List<PersonalExpenseUi> = emptyList(),
    val notes: String = "",

    val dateError: String? = null,
    val fuelError: String? = null,
    val vehicleError: String? = null,
    val refreshmentError: String? = null,

    val isSaving: Boolean = false
)

sealed interface ExpenseFormUiEvent {
    data object OnDateClick : ExpenseFormUiEvent

    data class OnDateSelected(val date: LocalDate) : ExpenseFormUiEvent

    data class OnFuelChanged(val value: String) : ExpenseFormUiEvent
    data class OnVehicleChanged(val value: String) : ExpenseFormUiEvent
    data class OnRefreshmentChanged(val value: String) : ExpenseFormUiEvent

    data object OnAddPersonalExpense : ExpenseFormUiEvent
    data class OnRemovedPersonalExpense(val id: String) : ExpenseFormUiEvent
    data class OnPersonalTitleChanged(val id: String, val value: String) : ExpenseFormUiEvent
    data class OnPersonalAmountChanged(val id: String, val value: String) : ExpenseFormUiEvent

    data class OnNotesChanged(val value: String) : ExpenseFormUiEvent

    data object OnSaveClicked : ExpenseFormUiEvent

}

sealed interface ExpenseFormUiEffect {
    data object OpenDatePicker : ExpenseFormUiEffect

    data object ExpenseSaved : ExpenseFormUiEffect

    data class ShowSnackbar(val message: String) : ExpenseFormUiEffect
}

