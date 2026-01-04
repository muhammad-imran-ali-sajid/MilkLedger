package com.miassolutions.milkledger.features.account.form

import com.miassolutions.milkledger.core.localdb.account.local.AccountType

data class AccountFormUiState(
    val sortOrder: String = "",
    val personName: String = "",
    val selectAccountType: AccountType = AccountType.CUSTOMER,
    val rate: String = "",
    val initialBalance: String = "",
    val advanceAmount: String = "",
    val isSaving: Boolean = false,
    val validation: AccountFormValidation = AccountFormValidation()
)

data class AccountFormValidation(
    val sortOrderError: String? = null,
    val nameError: String? = null,
    val rateError: String? = null,
    val type: String? = null,
    val initialBalanceError: String? = null,
    val isValid: Boolean = false
)

sealed interface AccountFormEvent {
    object SaveClicked : AccountFormEvent
    object CancelClicked : AccountFormEvent
}


sealed interface AccountFormEffect {
    object CloseScreen : AccountFormEffect
    data class ShowToast(val message: String) : AccountFormEffect
    data class FocusField(val field: Field) : AccountFormEffect
}

enum class Field {
    SORT_ORDER,
    NAME,
    ACCOUNT_TYPE,
    RATE,
    INITIAL_BALANCE
}


