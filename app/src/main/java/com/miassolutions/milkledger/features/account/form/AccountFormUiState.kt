package com.miassolutions.milkledger.features.account.form

import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import java.time.LocalDate

data class AccountFormUiState(
    val isEditMode: Boolean = false,
    val sortOrder: String = "",
    val personName: String = "",
    val selectAccountType: AccountType = AccountType.CUSTOMER,
    val rate: String = "",
    val initialBalance: String = "",
    val openingDate: LocalDate = LocalDate.now(),
    val advanceAmount: String = "",

    val isActive: Boolean = true, // 🔥 For Switch
    val showDeleteButton: Boolean = false, // 🔥 For Delete Visibility,

    val isSaving: Boolean = false,
    val validation: AccountFormValidation = AccountFormValidation()
)

data class AccountFormValidation(
    val sortOrderError: String? = null,
    val nameError: String? = null,
    val rateError: String? = null,
    val isValid: Boolean = false
)

sealed interface AccountFormEvent {
    object SaveClicked : AccountFormEvent
    object CancelClicked : AccountFormEvent

    data class OnActiveStatusChanged(val isActive: Boolean) : AccountFormEvent
    object DeleteClicked : AccountFormEvent // 🔥 Delete button press

    object OnOpeningDateClicked : AccountFormEvent

    data class OnOpeningDateSelected(val date: LocalDate) : AccountFormEvent

}


sealed interface AccountFormEffect {
    object CloseScreen : AccountFormEffect
    data class ShowToast(val message: String) : AccountFormEffect
    data class FocusField(val field: Field) : AccountFormEffect

    data class OpenDatePicker(val currentDate: LocalDate) : AccountFormEffect

    data class ShowDeleteConfirmation(val accountName: String) : AccountFormEffect
    data class ShowBalanceError(val balance: Long) : AccountFormEffect

}

enum class Field {
    SORT_ORDER,
    NAME,
    ACCOUNT_TYPE,
    RATE,
    INITIAL_BALANCE
}


