package com.miassolutions.milkledger.features.cashflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

// ---------- ViewModel ----------
class FilterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<FilterUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // --- Update selected type ---
    fun onTypeSelected(type: FilterType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        checkApplyEnabled()
    }

    // --- Update dates ---
    fun onFromDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(fromDate = date)
        checkApplyEnabled()
    }

    fun onToDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(toDate = date)
        checkApplyEnabled()
    }

    // --- Apply filter ---
    fun onApplyClicked() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiEvent.send(
                FilterUiEvent.ApplyFilter(
                    fromDate = state.fromDate,
                    toDate = state.toDate,
                    selectedType = state.selectedType
                )
            )
        }
    }

    // --- Reset filter ---
    fun onResetClicked() {
        _uiState.value = FilterUiState()
        viewModelScope.launch {
            _uiEvent.send(FilterUiEvent.ResetFilter)
        }
    }

    // --- Helper to enable/disable Apply button ---
    private fun checkApplyEnabled() {
        val state = _uiState.value
        _uiState.value = state.copy(
            isApplyEnabled = (state.fromDate != null && state.toDate != null)
        )
    }

    fun onDismiss() {
        viewModelScope.launch {
            _uiEvent.send(FilterUiEvent.Dismiss)
        }
    }
}