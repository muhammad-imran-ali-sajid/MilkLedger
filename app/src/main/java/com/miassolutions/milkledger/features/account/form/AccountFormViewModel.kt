package com.miassolutions.milkledger.features.account.form

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.features.account.data.AccountRepository
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
) : BaseViewModel<AccountFormUiState, AccountFormEvent, AccountFormEffect>(
    AccountFormUiState()
) {

    private val accountId: String? = savedStateHandle["accountId"]

    init {
        updateState { it.copy(isEditMode = accountId != null) }
        if (accountId != null) loadAccount()
    }

    // --------------------------------------------------
    // Load Existing Account (Edit Mode)
    // --------------------------------------------------

    private fun loadAccount() {
        viewModelScope.launch {
            val account = repository.getAccountById(accountId!!) ?: return@launch

            val openingDate =
                repository.getOpeningDate(accountId)
                    ?: account.createdDate

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
                    openingDate = openingDate
                )
            }
        }
    }

    // --------------------------------------------------
    // Events
    // --------------------------------------------------

    override fun onEvent(event: AccountFormEvent) {
        when (event) {

            AccountFormEvent.CancelClicked ->
                emitEffect(AccountFormEffect.CloseScreen)

            AccountFormEvent.SaveClicked ->
                onSave()

            AccountFormEvent.OnOpeningDateClicked ->
                emitEffect(AccountFormEffect.OpenDatePicker(currentState.openingDate))

            is AccountFormEvent.OnOpeningDateSelected ->
                updateState { it.copy(openingDate = event.date) }

            AccountFormEvent.DeleteClicked ->
                checkAndDelete()

            is AccountFormEvent.OnActiveStatusChanged ->
                updateState { it.copy(isActive = event.isActive) }
        }
    }

    // --------------------------------------------------
    // Save Logic
    // --------------------------------------------------

    private fun onSave() {
        viewModelScope.launch {

            val validation = validateInputs()
            if (!validation.isValid) {
                handleValidationErrors(validation)
                updateState { it.copy(validation = validation) }
                return@launch
            }

            updateState { it.copy(isSaving = true) }

            try {
                val sortOrder = currentState.sortOrder.toInt()
                val type = currentState.selectAccountType

                //  Business rule check (DB)
                if (repository.isSortOrderExist(sortOrder, type, accountId)) {
                    updateState {
                        it.copy(
                            validation = AccountFormValidation(
                                sortOrderError = "Sort order already exists"
                            ),
                            isSaving = false
                        )
                    }
                    emitEffect(AccountFormEffect.FocusField(Field.SORT_ORDER))
                    return@launch
                }

                val id = accountId ?: UUID.randomUUID().toString()

                val account = currentState.toDomain(id).copy(
                    isActive = currentState.isActive
                )

                repository.saveAccount(account, currentState.openingDate)

                emitEffect(AccountFormEffect.ShowToast("Account saved successfully"))
                emitEffect(AccountFormEffect.CloseScreen)

            } catch (e: Exception) {
                emitEffect(
                    AccountFormEffect.ShowToast(
                        e.localizedMessage ?: "Failed to save account"
                    )
                )
            } finally {
                updateState { it.copy(isSaving = false) }
            }
        }
    }

    // --------------------------------------------------
    // Validation (PURE)
    // --------------------------------------------------

    private fun validateInputs(): AccountFormValidation {
        val state = currentState

        if (state.sortOrder.toIntOrNull() == null) {
            return AccountFormValidation(sortOrderError = "Sort Order required")
        }

        if (state.personName.isBlank()) {
            return AccountFormValidation(nameError = "Name required")
        }

        if (state.rate.isBlank()) {
            return AccountFormValidation(rateError = "Rate required")
        }

        return AccountFormValidation(isValid = true)
    }

    private fun handleValidationErrors(validation: AccountFormValidation) {
        when {
            validation.sortOrderError != null ->
                emitEffect(AccountFormEffect.FocusField(Field.SORT_ORDER))

            validation.nameError != null ->
                emitEffect(AccountFormEffect.FocusField(Field.NAME))

            validation.rateError != null ->
                emitEffect(AccountFormEffect.FocusField(Field.RATE))
        }
    }

    // --------------------------------------------------
    // Delete Logic
    // --------------------------------------------------

    private fun checkAndDelete() {
        viewModelScope.launch {
            if (accountId == null) return@launch

            val balance = repository.getCurrentBalance(accountId)

            if (balance != 0L) {
                emitEffect(AccountFormEffect.ShowBalanceError(balance))
            } else {
                emitEffect(
                    AccountFormEffect.ShowDeleteConfirmation(currentState.personName)
                )
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

    // --------------------------------------------------
    // Field Setters
    // --------------------------------------------------

    fun onSortOrderChanged(value: String) =
        updateState { it.copy(sortOrder = value) }

    fun onNameChanged(value: String) =
        updateState { it.copy(personName = value) }

    fun onAccountTypeSelected(value: AccountType) =
        updateState { it.copy(selectAccountType = value) }

    fun onRateChanged(value: String) =
        updateState { it.copy(rate = value) }

    fun onInitialBalanceChanged(value: String) =
        updateState { it.copy(initialBalance = value) }

    fun onAdvanceAmountChanged(value: String) =
        updateState { it.copy(advanceAmount = value) }
}
