package com.miassolutions.milkledger.features.account.form

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.account.repository.AccountRepository
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.account.mapper.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountFormViewModel @Inject
constructor(private val repository: AccountRepository) :
    BaseViewModel<AccountFormUiState, AccountFormEvent, AccountFormEffect>(AccountFormUiState()) {

    private suspend fun validate(): AccountFormValidation {
        val state = currentState

        val sort = state.sortOrder.toIntOrNull()
            ?: return AccountFormValidation(sortOrderError = "Sort Order required")

        val type = state.selectAccountType
            ?: AccountFormValidation(type = "Account type requrired")

        if (repository.isSortOrderExist(sort, type as AccountType)) {
            return AccountFormValidation(
                sortOrderError = "Sort order already exists for this account type"
            )
        }

        if (state.personName.isBlank()) {
            return AccountFormValidation(nameError = "Name required")
        }

        if (state.selectAccountType == null) {
            return AccountFormValidation(nameError = "Account type required")
        }

        if (state.rate.isBlank()) {
            return AccountFormValidation(rateError = "Rate required")
        }

        if (state.initialBalance.isBlank()) {
            return AccountFormValidation(
                initialBalanceError = "Initial balance required"
            )
        }

        return AccountFormValidation(isValid = true)
    }


    override fun onEvent(event: AccountFormEvent) {
        when (event) {
            AccountFormEvent.CancelClicked -> emitEffect(AccountFormEffect.CloseScreen)
            AccountFormEvent.SaveClicked -> onSave()
        }
    }

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
                updateState {
                    it.copy(
                        validation = validation,
                        isSaving = false
                    )
                }
                return@launch
            }

            try {
                val account = currentState.toDomain()
                repository.saveAccount(account)
                AccountFormEffect.ShowToast("Account saved in db")
                emitEffect(AccountFormEffect.CloseScreen)
            } catch (e: Exception) {
                emitEffect(
                    AccountFormEffect.ShowToast("${e.localizedMessage}: Failed to save account")
                )
            } finally {
                updateState { it.copy(isSaving = false) }
            }

        }

    }
}