package com.miassolutions.milkledger.presentation.customer.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.presentation.customer.model.CustomerUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerFormViewModel @Inject constructor(
    private val repository: CustomerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editingCustomer: CustomerUi? = savedStateHandle["customer"]

    private val _uiState = MutableStateFlow(
        CustomerFormUiState(
            name = editingCustomer?.name.orEmpty(),
            rate = editingCustomer?.rate?.toString().orEmpty(),
            position = editingCustomer?.sortOrder?.toString().orEmpty(),
            advanceAmount = editingCustomer?.advanceAmount?.toString().orEmpty(),
            isEdit = editingCustomer != null
        )
    )
    val uiState: StateFlow<CustomerFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CustomerFormUiEvent>()
    val uiEvent: SharedFlow<CustomerFormUiEvent> = _uiEvent.asSharedFlow()

    private var allCustomers: List<Customer> = emptyList()

    init {
        // Observe all customers for duplicate sortOrder check
        viewModelScope.launch {
            repository.getAllCustomers()
                .collect { customers ->
                    allCustomers = customers
                }
        }
    }

    // -------------------------
    // Input handlers
    // -------------------------
    fun onNameChanged(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onRateChanged(value: String) =
        _uiState.update { it.copy(rate = value, rateError = null) }

    fun onPositionChanged(value: String) =
        _uiState.update { it.copy(position = value, positionError = null) }

    fun onAdvanceAmountChanged(value: String) =
        _uiState.update { it.copy(advanceAmount = value) }

    // -------------------------
    // Save
    // -------------------------
    fun onSaveClicked() {
        val state = _uiState.value
        val rate = state.rate.toDoubleOrNull()
        val position = state.position.toIntOrNull()

        var valid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            valid = false
        }

        if (rate == null) {
            _uiState.update { it.copy(rateError = "Invalid rate") }
            valid = false
        }

        if (position == null) {
            _uiState.update { it.copy(positionError = "Invalid position") }
            valid = false
        } else if (allCustomers.any { it.sortOrder == position && it.id != editingCustomer?.id }) {
            _uiState.update { it.copy(positionError = "Sort order already exists") }
            valid = false
        }

        if (!valid) return

        val customer = Customer(
            id = editingCustomer?.id ?: java.util.UUID.randomUUID().toString(),
            name = state.name.trim(),
            rate = rate!!,
            sortOrder = position!!,
            advanceAmount = state.advanceAmount.toDoubleOrNull() ?: 0.0,
            isDefault = editingCustomer?.isDefault ?: false
        )

        viewModelScope.launch {
            try {
                repository.upsertCustomer(customer)
                _uiEvent.emit(CustomerFormUiEvent.Dismiss)
            } catch (e: IllegalArgumentException) {
                // Handle duplicate sortOrder at repository level
                _uiState.update { it.copy(positionError = e.message) }
            }
        }
    }
}
