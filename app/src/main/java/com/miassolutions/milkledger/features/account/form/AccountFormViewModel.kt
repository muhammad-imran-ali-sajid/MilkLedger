package com.miassolutions.milkledger.features.account.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.account.domain.usecase.*
import com.miassolutions.milkledger.features.account.form.AccountFormEffect.*
import com.miassolutions.milkledger.features.account.mapper.toDomain
import com.miassolutions.milkledger.utils.extensions.toRupeesStr
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AccountFormViewModel @Inject constructor(
    private val saveAccount: SaveAccountUseCase,
    private val loadAccountForEdit: LoadAccountForEditUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<AccountFormUiState, AccountFormEvent, AccountFormEffect>(
    AccountFormUiState()
) {

    private val accountId: String? = savedStateHandle["accountId"]

    init {
        updateState { it.copy(isEditMode = accountId != null) }
        if (accountId != null) loadAccount()
    }

    /* --------------------------------------------------
     * Load (Edit Mode)
     * -------------------------------------------------- */

    private fun loadAccount() {
        viewModelScope.launch {
            val (account, openingDate) =
                loadAccountForEdit(accountId!!)

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

    /* --------------------------------------------------
     * Events
     * -------------------------------------------------- */

    override fun onEvent(event: AccountFormEvent) {
        when (event) {

            AccountFormEvent.SaveClicked ->
                onSave()

            AccountFormEvent.DeleteClicked ->
                onDelete()

            AccountFormEvent.CancelClicked ->
                emitEffect(CloseScreen)

            AccountFormEvent.OnOpeningDateClicked ->
                emitEffect(OpenDatePicker(currentState.openingDate))

            is AccountFormEvent.OnOpeningDateSelected ->
                updateState { it.copy(openingDate = event.date) }

            is AccountFormEvent.OnActiveStatusChanged ->
                updateState { it.copy(isActive = event.isActive) }
        }
    }

    /* --------------------------------------------------
     * Save
     * -------------------------------------------------- */

    private fun onSave() {
        viewModelScope.launch {

            val validation = validateInputs()
            if (!validation.isValid) {
                handleValidationErrors(validation)
                updateState { it.copy(validation = validation) }
                return@launch
            }

            updateState { it.copy(isSaving = true) }

            val id = accountId ?: UUID.randomUUID().toString()
            val account = currentState.toDomain(id).copy(
                isActive = currentState.isActive
            )

            when (
                saveAccount(
                    account = account,
                    openingDate = currentState.openingDate,
                    excludeId = accountId
                )
            ) {

                SaveAccountResult.Success -> {
                    emitEffect(ShowToast("Account saved successfully"))
                    emitEffect(CloseScreen)
                }

                SaveAccountResult.SortOrderExists -> {
                    updateState {
                        it.copy(
                            isSaving = false,
                            validation = AccountFormValidation(
                                sortOrderError = "Sort order already exists"
                            )
                        )
                    }
                    emitEffect(FocusField(Field.SORT_ORDER))
                }
            }

            updateState { it.copy(isSaving = false) }
        }
    }

    /* --------------------------------------------------
     * Delete
     * -------------------------------------------------- */

    private fun onDelete() {
        viewModelScope.launch {

            when (val result = deleteAccount(accountId!!)) {

                is DeleteAccountResult.BalanceNotZero ->
                    emitEffect(ShowBalanceError(result.balance))

                DeleteAccountResult.Deleted -> {
                    emitEffect(ShowToast("Account deleted"))
                    emitEffect(CloseScreen)
                }
            }
        }
    }

    /* --------------------------------------------------
     * Validation (UI-only)
     * -------------------------------------------------- */

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
                emitEffect(FocusField(Field.SORT_ORDER))

            validation.nameError != null ->
                emitEffect(FocusField(Field.NAME))

            validation.rateError != null ->
                emitEffect(FocusField(Field.RATE))
        }
    }

    /* --------------------------------------------------
     * Field Setters (UI → State)
     * -------------------------------------------------- */

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
