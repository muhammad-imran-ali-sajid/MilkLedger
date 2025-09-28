package com.miassolutions.milkledger.core.ui

import android.os.Bundle

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class ShowToast(val message: String) : UiEvent
    data class Navigate(val destId: Int, val args: Bundle? = null) : UiEvent
    data object NavigateBack : UiEvent
}