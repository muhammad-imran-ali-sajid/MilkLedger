package com.miassolutions.milkledger.features.expense.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.expense.data.repository.ExpenseRepository
import com.miassolutions.milkledger.features.expense.domain.Expense
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ExpenseFormViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<ExpenseFormUiState, ExpenseFormUiEvent, ExpenseFormUiEffect>(
    ExpenseFormUiState()
) {

    /*
     * Keeps the latest text entered in personal expense rows.
     *
     * Personal expense RecyclerView rows do not update the complete UiState
     * on every keystroke, which helps avoid unnecessary row re-binding.
     */
    private val draftCache = mutableMapOf<String, PersonalExpenseUi>()

    init {
        val dateMillis: Long = savedStateHandle["selectedDate"] ?: -1L

        val initialDate = if (dateMillis != -1L) {
            dateMillis.toLocalDate()
        } else {
            LocalDate.now()
        }

        updateState {
            it.copy(date = initialDate)
        }
    }

    override fun onEvent(event: ExpenseFormUiEvent) {
        when (event) {

            // Date
            ExpenseFormUiEvent.OnDateClick -> {
                emitEffect(ExpenseFormUiEffect.OpenDatePicker)
            }

            is ExpenseFormUiEvent.OnDateSelected -> {
                updateState {
                    it.copy(
                        date = event.date,
                        dateError = null
                    )
                }
            }


            // Business expenses
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

            is ExpenseFormUiEvent.OnOtherChanged -> {
                updateState {
                    it.copy(
                        otherBusiness = event.value,
                        otherBusinessError = null
                    )
                }
            }


            // Personal expenses
            ExpenseFormUiEvent.OnAddPersonalExpense -> {
                addPersonalExpense()
            }

            is ExpenseFormUiEvent.OnRemovedPersonalExpense -> {
                removePersonalExpense(event.id)
            }

            is ExpenseFormUiEvent.OnPersonalDraftChanged -> {
                updatePersonalDraft(
                    id = event.id,
                    field = event.field,
                    value = event.value
                )
            }

            is ExpenseFormUiEvent.OnPersonalCommit -> {
                commitPersonalExpense(event.id)
            }


            // Notes
            is ExpenseFormUiEvent.OnNotesChanged -> {
                updateState {
                    it.copy(notes = event.value)
                }
            }


            // Save
            ExpenseFormUiEvent.OnSaveClicked -> {
                saveData()
            }
        }
    }


    private fun addPersonalExpense() {
        val newItem = PersonalExpenseUi()

        draftCache[newItem.id] = newItem

        updateState {
            it.copy(
                personalExpenses = it.personalExpenses + newItem
            )
        }
    }

    private fun removePersonalExpense(id: String) {
        draftCache.remove(id)

        updateState { state ->
            state.copy(
                personalExpenses = state.personalExpenses.filter {
                    it.id != id
                }
            )
        }
    }

    private fun updatePersonalDraft(
        id: String,
        field: DraftField,
        value: String
    ) {
        val current = draftCache[id] ?: return

        draftCache[id] = when (field) {
            DraftField.TITLE -> {
                current.copy(
                    title = value,
                    titleError = null
                )
            }

            DraftField.AMOUNT -> {
                current.copy(
                    amount = value,
                    amountError = null
                )
            }
        }
    }

    private fun commitPersonalExpense(id: String) {
        val draft = draftCache[id] ?: return

        updateState { state ->
            state.copy(
                personalExpenses = state.personalExpenses.map { item ->
                    if (item.id == id) {
                        draft
                    } else {
                        item
                    }
                }
            )
        }
    }


    private fun saveData() {
        val state = currentState
        val date = state.date
        val note = state.notes

        viewModelScope.launch {

            updateState {
                it.copy(isSaving = true)
            }

            val expensesToSave = mutableListOf<Expense>()


            // Business expenses
            parseAndAdd(
                list = expensesToSave,
                date = date,
                category = "Fuel",
                defaultTitle = "Fuel",
                amountStr = state.fuelAmount,
                note = note
            )

            parseAndAdd(
                list = expensesToSave,
                date = date,
                category = "Vehicle",
                defaultTitle = "Vehicle",
                amountStr = state.vehicleAmount,
                note = note
            )

            parseAndAdd(
                list = expensesToSave,
                date = date,
                category = "Refreshment",
                defaultTitle = "Refreshment",
                amountStr = state.refreshmentAmount,
                note = note
            )

            parseAndAdd(
                list = expensesToSave,
                date = date,
                category = "Other",
                defaultTitle = "Other",
                amountStr = state.otherBusiness,
                note = note
            )


            // Personal expenses
            //
            // Use draftCache because it always contains the latest typed value,
            // even when the EditText still has focus.
            val currentIds = state.personalExpenses
                .map { it.id }
                .toSet()

            draftCache.values
                .filter { it.id in currentIds }
                .forEach { item ->

                    val amount = item.amount.toDoubleOrNull()

                    if (amount != null && amount > 0) {

                        val title = item.title.ifBlank {
                            "Personal Expense"
                        }

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
                updateState {
                    it.copy(isSaving = false)
                }

                emitEffect(
                    ExpenseFormUiEffect.ShowSnackbar(
                        "Please enter at least one amount"
                    )
                )

                return@launch
            }


            try {
                withContext(NonCancellable) {
                    repository.saveAllExpenses(expensesToSave)
                }

                emitEffect(
                    ExpenseFormUiEffect.ShowSnackbar(
                        "Saved successfully"
                    )
                )

                emitEffect(
                    ExpenseFormUiEffect.ExpenseSaved
                )

            } catch (e: Exception) {

                updateState {
                    it.copy(isSaving = false)
                }

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
        val amount = amountStr.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            return
        }

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