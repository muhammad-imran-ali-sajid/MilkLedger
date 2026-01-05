package com.miassolutions.milkledger.features.expense.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseFormViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<ExpenseFormUiState, ExpenseFormUiEvent, ExpenseFormUiEffect>(ExpenseFormUiState()) {


    init {
        val dateMillis: Long = savedStateHandle["selectedDate"] ?: -1L

        val initialDate = if (dateMillis != -1L){
            dateMillis.toLocalDate()
        } else {
            LocalDate.now()
        }

        updateState { it.copy(date = initialDate) }
    }

    override fun onEvent(event: ExpenseFormUiEvent) {
        when (event) {
            is ExpenseFormUiEvent.OnDateClick -> emitEffect(ExpenseFormUiEffect.OpenDatePicker)
            is ExpenseFormUiEvent.OnDateSelected -> updateState { it.copy(date = event.date) }
            is ExpenseFormUiEvent.OnFuelChanged -> updateState {
                it.copy(
                    fuelAmount = event.value,
                    fuelError = null
                )
            }

            is ExpenseFormUiEvent.OnVehicleChanged -> updateState {
                it.copy(
                    vehicleAmount = event.value,
                    vehicleError = null
                )
            }

            is ExpenseFormUiEvent.OnRefreshmentChanged -> updateState {
                it.copy(
                    refreshmentAmount = event.value,
                    refreshmentError = null
                )
            }

            is ExpenseFormUiEvent.OnNotesChanged -> updateState { it.copy(notes = event.value) }


            is ExpenseFormUiEvent.OnAddPersonalExpense -> {
                val newItem = PersonalExpenseUi()
                updateState { it.copy(personalExpenses = it.personalExpenses + newItem) }
            }

            is ExpenseFormUiEvent.OnRemovedPersonalExpense -> {
                updateState { state ->
                    state.copy(personalExpenses = state.personalExpenses.filter { it.id != event.id })
                }
            }

            is ExpenseFormUiEvent.OnPersonalTitleChanged -> {
                updatePersonalItem(event.id) { it.copy(title = event.value, titleError = null) }
            }

            is ExpenseFormUiEvent.OnPersonalAmountChanged -> {
                updatePersonalItem(event.id) { it.copy(amount = event.value, amountError = null) }
            }

            is ExpenseFormUiEvent.OnSaveClicked -> saveData()

        }


    }

    private fun saveData() {
        val state = currentState
        val date = state.date
        val note = state.notes

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }

            val expensesToSave = mutableListOf<Expense>()

            //static
            parseAndAdd(
                expensesToSave,
                date,
                "Fuel",
                "Fuel Expense",
                state.fuelAmount,
                note
            )
            parseAndAdd(
                expensesToSave,
                date,
                "Vehicle",
                "Vehicle Maintenance",
                state.vehicleAmount,
                note
            )
            parseAndAdd(
                expensesToSave,
                date,
                "Refreshment",
                "Refreshment",
                state.refreshmentAmount,
                note
            )

            //dynamic
            state.personalExpenses.forEach { item ->
                val amount = item.amount.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    val title = item.title.ifBlank { "Personal Expense" }
                    expensesToSave.add(
                        Expense(
                            date = date,
                            title = title,
                            category = "Personal",
                            amount = (amount * 100).toLong(),
                            isPersonal = true,
                            note = note
                        )
                    )
                }
            }

            if (expensesToSave.isEmpty()) {
                updateState { it.copy(isSaving = false) }
                emitEffect(ExpenseFormUiEffect.ShowSnackbar("Please enter at least on amount"))
                return@launch
            }

            try {
                repository.saveAllExpenses(expensesToSave)
                emitEffect(ExpenseFormUiEffect.ShowSnackbar("Saved successfully"))
                emitEffect(ExpenseFormUiEffect.ExpenseSaved)
            } catch (e: Exception) {
                emitEffect(ExpenseFormUiEffect.ShowSnackbar("Error: ${e.message}"))
                updateState { it.copy(isSaving = false) }
            }

        }
    }

    private fun parseAndAdd(
        list: MutableList<Expense>,
        date: LocalDate,
        category: String,
        defaultTitle: String,
        amountStr: String,
        note: String
    ) {
        amountStr.toDoubleOrNull()?.let { amount ->
            if (amount > 0) {
                list.add(
                    Expense(
                        date = date,
                        title = defaultTitle,
                        amount = (amount * 100).toLong(),
                        category = category,
                        isPersonal = false,
                        note = note
                    )
                )
            }
        }
    }

    private fun updatePersonalItem(
        id: String,
        updateBlock: (PersonalExpenseUi) -> PersonalExpenseUi
    ) {
        updateState { state ->
            state.copy(personalExpenses = state.personalExpenses.map { item ->
                if (item.id == id) updateBlock(item) else item
            })
        }
    }


}
