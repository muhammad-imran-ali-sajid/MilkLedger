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
) : BaseViewModel<
        ExpenseFormUiState,
        ExpenseFormUiEvent,
        ExpenseFormUiEffect
        >(ExpenseFormUiState()) {

    /**
     * 🔹 Draft cache
     * Typing yahan hoti hai, RecyclerView redraw nahi hota
     */
    private val draftCache = mutableMapOf<String, PersonalExpenseUi>()

    init {
        val dateMillis: Long = savedStateHandle["selectedDate"] ?: -1L

        val initialDate = if (dateMillis != -1L) {
            dateMillis.toLocalDate()
        } else {
            LocalDate.now()
        }

        updateState { it.copy(date = initialDate) }
    }

    override fun onEvent(event: ExpenseFormUiEvent) {
        when (event) {

            // ---------------- DATE ----------------

            is ExpenseFormUiEvent.OnDateClick -> {
                emitEffect(ExpenseFormUiEffect.OpenDatePicker)
            }

            is ExpenseFormUiEvent.OnDateSelected -> {
                updateState { it.copy(date = event.date) }
            }

            // ---------------- STATIC FIELDS ----------------

            is ExpenseFormUiEvent.OnFuelChanged -> {
                updateState {
                    it.copy(
                        fuelAmount = event.value,
                        fuelError = null
                    )
                }
            }

            is ExpenseFormUiEvent.OnVehicleChanged -> {
                updateState {
                    it.copy(
                        vehicleAmount = event.value,
                        vehicleError = null
                    )
                }
            }

            is ExpenseFormUiEvent.OnRefreshmentChanged -> {
                updateState {
                    it.copy(
                        refreshmentAmount = event.value,
                        refreshmentError = null
                    )
                }
            }

            is ExpenseFormUiEvent.OnNotesChanged -> {
                updateState { it.copy(notes = event.value) }
            }

            // ---------------- PERSONAL EXPENSE (DYNAMIC) ----------------

            is ExpenseFormUiEvent.OnAddPersonalExpense -> {
                val newItem = PersonalExpenseUi()
                draftCache[newItem.id] = newItem
                updateState {
                    it.copy(
                        personalExpenses = it.personalExpenses + newItem
                    )
                }
            }

            is ExpenseFormUiEvent.OnRemovedPersonalExpense -> {
                draftCache.remove(event.id)
                updateState { state ->
                    state.copy(
                        personalExpenses = state.personalExpenses.filter {
                            it.id != event.id
                        }
                    )
                }
            }

            /**
             * 🔹 Draft typing
             * NO state emit, NO RecyclerView redraw
             */
            is ExpenseFormUiEvent.OnPersonalDraftChanged -> {
                val current = draftCache[event.id] ?: return
                draftCache[event.id] = when (event.field) {
                    DraftField.TITLE -> current.copy(title = event.value)
                    DraftField.AMOUNT -> current.copy(amount = event.value)
                }
            }

            /**
             * 🔹 Commit
             * Yahan actual list update hoti hai
             */
            is ExpenseFormUiEvent.OnPersonalCommit -> {
                val draft = draftCache[event.id] ?: return
                updateState { state ->
                    state.copy(
                        personalExpenses = state.personalExpenses.map {
                            if (it.id == event.id) draft else it
                        }
                    )
                }
            }

            // ---------------- SAVE ----------------

            is ExpenseFormUiEvent.OnSaveClicked -> {
                saveData()
            }


        }
    }

    // ---------------- SAVE LOGIC ----------------

    private fun saveData() {
        val state = currentState
        val date = state.date
        val note = state.notes

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }

            val expensesToSave = mutableListOf<Expense>()

            // ---- Static expenses ----
            parseAndAdd(
                expensesToSave,
                date,
                "Fuel",
                "Fuel",
                state.fuelAmount,
                note
            )

            parseAndAdd(
                expensesToSave,
                date,
                "Vehicle",
                "Vehicle",
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

            // ---- Personal expenses ----
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
                emitEffect(
                    ExpenseFormUiEffect.ShowSnackbar(
                        "Please enter at least one amount"
                    )
                )
                return@launch
            }

            try {
                repository.saveAllExpenses(expensesToSave)
                emitEffect(ExpenseFormUiEffect.ShowSnackbar("Saved successfully"))
                emitEffect(ExpenseFormUiEffect.ExpenseSaved)
            } catch (e: Exception) {
                updateState { it.copy(isSaving = false) }
                emitEffect(
                    ExpenseFormUiEffect.ShowSnackbar(
                        "Error: ${e.message}"
                    )
                )
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
                        category = category,
                        amount = (amount * 100).toLong(),
                        isPersonal = false,
                        note = note
                    )
                )
            }
        }
    }
}
