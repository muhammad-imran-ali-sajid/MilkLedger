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

    // 🔹 Single Source of Truth for "Current Editing Data"
    private val draftCache = mutableMapOf<String, PersonalExpenseUi>()

    init {
        val dateMillis: Long = savedStateHandle["selectedDate"] ?: -1L
        val initialDate = if (dateMillis != -1L) dateMillis.toLocalDate() else LocalDate.now()
        updateState { it.copy(date = initialDate) }
    }

    override fun onEvent(event: ExpenseFormUiEvent) {
        when (event) {
            // --- Date ---
            is ExpenseFormUiEvent.OnDateClick -> emitEffect(ExpenseFormUiEffect.OpenDatePicker)
            is ExpenseFormUiEvent.OnDateSelected -> updateState { it.copy(date = event.date) }

            // --- Static Fields ---
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
            is ExpenseFormUiEvent.OnOtherChanged -> updateState {
                it.copy(
                    otherBusiness = event.value,
                    otherBusinessError = null
                )
            }

            // --- Personal Expense Logic ---
            is ExpenseFormUiEvent.OnAddPersonalExpense -> {
                val newItem = PersonalExpenseUi()
                // 1. Cache me add karen
                draftCache[newItem.id] = newItem
                // 2. State update karen (UI me row add hogi)
                updateState { it.copy(personalExpenses = it.personalExpenses + newItem) }
            }

            is ExpenseFormUiEvent.OnRemovedPersonalExpense -> {
                // 1. Cache se remove
                draftCache.remove(event.id)
                // 2. State se remove
                updateState { state ->
                    state.copy(personalExpenses = state.personalExpenses.filter { it.id != event.id })
                }
            }

            is ExpenseFormUiEvent.OnPersonalDraftChanged -> {
                // Sirf Cache update hoga (No Re-render, No flickering)
                val current = draftCache[event.id] ?: return
                draftCache[event.id] = when (event.field) {
                    DraftField.TITLE -> current.copy(title = event.value)
                    DraftField.AMOUNT -> current.copy(amount = event.value)
                }
            }

            is ExpenseFormUiEvent.OnPersonalCommit -> {
                // Jab focus hatega, tab State sync hogi (Recycling k liye zaroori hai)
                val draft = draftCache[event.id] ?: return
                updateState { state ->
                    state.copy(personalExpenses = state.personalExpenses.map {
                        if (it.id == event.id) draft else it
                    })
                }
            }

            // --- Save ---
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

            // 1. Static Expenses
            parseAndAdd(expensesToSave, date, "Fuel", "Fuel", state.fuelAmount, note)
            parseAndAdd(expensesToSave, date, "Vehicle", "Vehicle", state.vehicleAmount, note)
            parseAndAdd(
                expensesToSave,
                date,
                "Refreshment",
                "Refreshment",
                state.refreshmentAmount,
                note
            )
            parseAndAdd(expensesToSave, date, "Other", "Other", state.otherBusiness, note)

            // 2. Personal Expenses (🔥 FIX: Use draftCache directly)
            // Hum 'state.personalExpenses' use nahi karenge kyunke ho sakta hai user ne abhi type kia ho
            // aur focus na hataya ho. 'draftCache' me hamesha latest typing hoti hai.

            // Hum sirf un items ko uthayenge jo abhi list me valid hain (deleted nahi hain)
            val currentIds = state.personalExpenses.map { it.id }.toSet()

            draftCache.values.filter { it.id in currentIds }.forEach { item ->
                val amount = item.amount.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    val title = item.title.ifBlank { "Personal Expense" }
                    expensesToSave.add(
                        Expense(
                            date = date,
                            title = title,
                            category = "Personal",
                            amount = (amount * 100).toLong(),
                            isPersonal = true, // ✅ Repository flag check karega
                            note = note
                        )
                    )
                }
            }

            // 3. Final Check
            if (expensesToSave.isEmpty()) {
                updateState { it.copy(isSaving = false) }
                emitEffect(ExpenseFormUiEffect.ShowSnackbar("Please enter at least one amount"))
                return@launch
            }

            try {
                repository.saveAllExpenses(expensesToSave)
                emitEffect(ExpenseFormUiEffect.ShowSnackbar("Saved successfully"))
                emitEffect(ExpenseFormUiEffect.ExpenseSaved)
            } catch (e: Exception) {
                updateState { it.copy(isSaving = false) }
                emitEffect(ExpenseFormUiEffect.ShowSnackbar("Error: ${e.message}"))
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
        val amount = amountStr.toDoubleOrNull()
        if (amount != null && amount > 0) {
            list.add(
                Expense(
                    date = date,
                    title = defaultTitle,
                    category = category,
                    amount = (amount * 100).toLong(),
                    isPersonal = false, // Business Expense
                    note = note
                )
            )
        }
    }
}