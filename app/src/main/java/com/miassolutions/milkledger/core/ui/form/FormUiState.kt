package com.miassolutions.milkledger.core.ui.form

// UiState.kt
sealed class FormUiState {
    data object Idle : FormUiState()
    data object Loading : FormUiState()
    data class Success(val data: Map<String, String>) : FormUiState()
    data class Error(val message: String) : FormUiState()
}

// UiEvent.kt
sealed class FormUiEvent {
    data object Submit : FormUiEvent()
    data object Dismiss : FormUiEvent()
}
