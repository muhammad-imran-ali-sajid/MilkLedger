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

    // Business expenses
    val fuelAmount: String = "",
    val vehicleAmount: String = "",
    val refreshmentAmount: String = "",
    val otherBusiness: String = "",

    // Personal expenses
    val personalExpenses: List<PersonalExpenseUi> = emptyList(),
    val personalExpenseTotal: Double = 0.0,

    // Notes
    val notes: String = "",

    // Errors
    val dateError: String? = null,
    val fuelError: String? = null,
    val vehicleError: String? = null,
    val refreshmentError: String? = null,
    val otherBusinessError: String? = null,

    // Saving
    val isSaving: Boolean = false
) {

    val totalExpense: Double
        get() {
            val fuel = fuelAmount.toDoubleOrNull() ?: 0.0
            val vehicle = vehicleAmount.toDoubleOrNull() ?: 0.0
            val refreshment = refreshmentAmount.toDoubleOrNull() ?: 0.0
            val other = otherBusiness.toDoubleOrNull() ?: 0.0

            return fuel +
                    vehicle +
                    refreshment +
                    other +
                    personalExpenseTotal
        }
}

sealed interface ExpenseFormUiEvent {

    data object OnDateClick : ExpenseFormUiEvent

    data class OnDateSelected(
        val date: LocalDate
    ) : ExpenseFormUiEvent

    data class OnFuelChanged(
        val value: String
    ) : ExpenseFormUiEvent

    data class OnVehicleChanged(
        val value: String
    ) : ExpenseFormUiEvent

    data class OnRefreshmentChanged(
        val value: String
    ) : ExpenseFormUiEvent

    data class OnOtherChanged(
        val value: String
    ) : ExpenseFormUiEvent

    data class OnPersonalDraftChanged(
        val id: String,
        val field: DraftField,
        val value: String
    ) : ExpenseFormUiEvent

    data class OnPersonalCommit(
        val id: String
    ) : ExpenseFormUiEvent

    data object OnAddPersonalExpense : ExpenseFormUiEvent

    data class OnRemovedPersonalExpense(
        val id: String
    ) : ExpenseFormUiEvent

    data class OnNotesChanged(
        val value: String
    ) : ExpenseFormUiEvent

    data object OnSaveClicked : ExpenseFormUiEvent
}

sealed interface ExpenseFormUiEffect {

    data object OpenDatePicker : ExpenseFormUiEffect

    data object ExpenseSaved : ExpenseFormUiEffect

    data class ShowSnackbar(
        val message: String
    ) : ExpenseFormUiEffect
}