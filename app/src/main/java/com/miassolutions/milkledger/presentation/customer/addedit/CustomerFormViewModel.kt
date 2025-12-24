package com.miassolutions.milkledger.presentation.customer.addedit

import androidx.lifecycle.SavedStateHandle
import com.miassolutions.milkledger.data.repository.CustomerRepository



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.oldmapper.toEntity
import com.miassolutions.milkledger.domain.model.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerFormViewModel @Inject constructor(
    private val repository: CustomerRepository,
    savedStateHandle: SavedStateHandle

) : ViewModel() {

    private val customer: Customer? = savedStateHandle.get<Customer>("customer")
    private val currentCustomers: List<Customer> = savedStateHandle.get<List<Customer>>("currentCustomers") ?: emptyList()

    private val _uiState = MutableStateFlow(
        CustomerFormUiState(
            name = customer?.name.orEmpty(),
            rate = customer?.rate?.toString().orEmpty(),
            position = customer?.sortOrder?.toString().orEmpty(),
            advanceAmount = customer?.advanceAmount?.toString().orEmpty(),
            isEdit = customer != null
        )
    )
    val uiState: StateFlow<CustomerFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CustomerFormUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value, nameError = null) }
    }

    fun onRateChanged(value: String) {
        _uiState.update { it.copy(rate = value, rateError = null) }
    }

    fun onPositionChanged(value: String) {
        _uiState.update { it.copy(position = value, positionError = null) }
    }

    fun onAdvanceAmountChanged(value: String) {
        _uiState.update { it.copy(advanceAmount = value) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        var isValid = true

        val rate = state.rate.toDoubleOrNull()
        val position = state.position.toIntOrNull()

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            isValid = false
        }

        if (state.rate.isBlank()) {
            _uiState.update { it.copy(rateError = "Rate is required") }
            isValid = false
        } else if (rate == null) {
            _uiState.update { it.copy(rateError = "Invalid rate") }
            isValid = false
        }

        when {
            state.position.isBlank() -> {
                _uiState.update { it.copy(positionError = "Position is required") }
                isValid = false
            }
            position == null -> {
                _uiState.update { it.copy(positionError = "Invalid number") }
                isValid = false
            }
            currentCustomers.any { it.sortOrder == position && it.id != customer?.id } -> {
                _uiState.update { it.copy(positionError = "Sort order already exists") }
                isValid = false
            }
        }

        if (!isValid) return

        val updatedCustomer = Customer(
            id = customer?.id,
            name = state.name.trim(),
            rate = rate!!,
            sortOrder = position!!,
            advanceAmount = state.advanceAmount.toDoubleOrNull() ?: 0.0
        )

        viewModelScope.launch {
            repository.insertCustomer(updatedCustomer.toEntity())

            _uiEvent.emit(CustomerFormUiEvent.Dismiss)
        }
    }
}
