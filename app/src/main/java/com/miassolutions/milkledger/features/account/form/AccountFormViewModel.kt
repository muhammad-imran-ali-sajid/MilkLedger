package com.miassolutions.milkledger.features.account.form

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.repository.AccountRepository
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.account.form.AccountFormEffect.*
import com.miassolutions.milkledger.features.account.mapper.toDomain
import com.miassolutions.milkledger.utils.extensions.toLocalDate // Extension function needed
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.extensions.toRupeesStr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AccountFormViewModel @Inject constructor(
    private val repository: AccountRepository,
    savedStateHandle: SavedStateHandle
) :
    BaseViewModel<AccountFormUiState, AccountFormEvent, AccountFormEffect>(AccountFormUiState()) {

    private val accountId: String? = savedStateHandle["accountId"]

    init {
        updateState { it.copy(isEditMode = accountId != null) }
        if (accountId != null) loadAccount()
    }

    private fun loadAccount() {
        viewModelScope.launch {
            val account = repository.getAccountById(accountId!!)
            Log.d("AccountEdit", "Loaded account = $account")
            if (account == null) return@launch

            val ledgerDate = repository.getOpeningDate(accountId)
            val finalDate = ledgerDate ?: account.createdAtMillis.toLocalDate()

            updateState {
                it.copy(
                    sortOrder = account.sortOrder.toString(),
                    personName = account.name,
                    selectAccountType = account.type,
                    rate = account.defaultRate.toString(),
                    initialBalance = account.initialBalance?.toRupeesStr().orEmpty(),
                    advanceAmount = account.advanceAmount?.toRupeesStr().orEmpty(),
                    isActive = account.isActive,
                    showDeleteButton = true,

                    openingDate = finalDate
                )
            }
        }
    }

    private suspend fun validate(): AccountFormValidation {
        val state = currentState
        val sort = state.sortOrder.toIntOrNull()
            ?: return AccountFormValidation(sortOrderError = "Sort Order required")

        val type = state.selectAccountType

        if (repository.isSortOrderExist(sort, type, accountId)) {
            emitEffect(AccountFormEffect.FocusField(Field.SORT_ORDER))
            return AccountFormValidation(
                sortOrderError = "Sort order already exists for this account type"
            )
        }

        if (state.personName.isBlank()) {
            emitEffect(AccountFormEffect.FocusField(Field.NAME))
            return AccountFormValidation(nameError = "Name required")
        }

        if (state.rate.isBlank()) {
            emitEffect(AccountFormEffect.FocusField(Field.RATE))
            return AccountFormValidation(rateError = "Rate required")
        }



        return AccountFormValidation(isValid = true)
    }

    override fun onEvent(event: AccountFormEvent) {
        when (event) {
            AccountFormEvent.CancelClicked -> emitEffect(AccountFormEffect.CloseScreen)
            AccountFormEvent.SaveClicked -> onSave()

            // 🔥 NEW: Handle Date Events
            AccountFormEvent.OnOpeningDateClicked -> {
                // Pass current selected date in millis to picker
                emitEffect(OpenDatePicker(currentState.openingDate.toMillis()))
            }

            is AccountFormEvent.OnOpeningDateSelected -> {
                updateState { it.copy(openingDate = event.date) }
            }

            AccountFormEvent.DeleteClicked -> {
                checkAndDelete()
            }

            is AccountFormEvent.OnActiveStatusChanged -> {
                updateState { it.copy(isActive = event.isActive) }
            }
        }
    }

    private fun checkAndDelete() {
        viewModelScope.launch {
            if (accountId == null) return@launch // New account delete nhi hota

            // A. Balance Check Karen
            val currentBalance = repository.getCurrentBalance(accountId)

            if (currentBalance != 0L) {
                // Balance Zero nahi hai -> Error dikhayen
                emitEffect(AccountFormEffect.ShowBalanceError(currentBalance))
            } else {
                // Balance Zero hai -> Confirmation maangen
                val name = currentState.personName
                emitEffect(AccountFormEffect.ShowDeleteConfirmation(name))
            }
        }
    }


    fun confirmDelete() {
        viewModelScope.launch {
            repository.deleteAccount(accountId!!)
            emitEffect(AccountFormEffect.ShowToast("Account deleted"))
            emitEffect(AccountFormEffect.CloseScreen)
        }
    }


    // ... Other setters same as before ...
    fun onSortOrderChanged(value: String) = updateState { it.copy(sortOrder = value) }
    fun onNameChanged(value: String) = updateState { it.copy(personName = value) }
    fun onAccountTypeSelected(value: AccountType) =
        updateState { it.copy(selectAccountType = value) }

    fun onRateChanged(value: String) = updateState { it.copy(rate = value) }
    fun onInitialBalanceChanged(value: String) = updateState { it.copy(initialBalance = value) }
    fun onAdvanceAmountChanged(value: String) = updateState { it.copy(advanceAmount = value) }

    private fun onSave() {
        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }

            val validation = validate()
            if (!validation.isValid) {
                updateState { it.copy(validation = validation, isSaving = false) }
                return@launch
            }

            try {
                val id = accountId ?: UUID.randomUUID().toString()

                // Note: make sure toDomain() exists and handles basic fields
                val account = currentState.toDomain(id).copy(
                    isActive = currentState.isActive
                )

                // 🔥 CRITICAL CHANGE: Pass Date to Repository
                repository.saveAccount(account, currentState.openingDate)

                emitEffect(AccountFormEffect.ShowToast("Account saved successfully"))
                emitEffect(AccountFormEffect.CloseScreen)
            } catch (e: Exception) {
                emitEffect(AccountFormEffect.ShowToast("${e.localizedMessage}: Failed to save account"))
            } finally {
                updateState { it.copy(isSaving = false) }
            }
        }
    }
}