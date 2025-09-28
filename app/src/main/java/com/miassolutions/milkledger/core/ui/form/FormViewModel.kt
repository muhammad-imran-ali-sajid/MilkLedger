package com.miassolutions.milkledger.core.ui.form


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow<FormUiState>(FormUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: FormUiEvent, fields: Map<String, String> = emptyMap()) {
        when (event) {
            is FormUiEvent.Submit -> {
                viewModelScope.launch {
                    if (fields.values.any { it.isBlank() }) {
                        _uiState.update { FormUiState.Error("All fields are required") }
                    } else {
                        _uiState.update { FormUiState.Loading }
                        // Simulate saving
                        kotlinx.coroutines.delay(500)
                        _uiState.update { FormUiState.Success(fields) }
                    }
                }
            }
            is FormUiEvent.Dismiss -> {
                _uiState.update { FormUiState.Idle }
            }
        }
    }
}
