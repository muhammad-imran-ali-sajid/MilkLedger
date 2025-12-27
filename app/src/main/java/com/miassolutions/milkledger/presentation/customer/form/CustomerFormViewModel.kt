package com.miassolutions.milkledger.presentation.customer.form


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.presentation.customer.customers.model.CustomerUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerFormViewModel @Inject constructor(
    private val repository: CustomerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editingCustomer: CustomerUi? =
        savedStateHandle["customer"]

    private val existingCustomers: List<CustomerUi> =
        savedStateHandle["currentCustomers"] ?: emptyList()

    private val _uiState = MutableStateFlow(
        CustomerFormUiState(
            name = editingCustomer?.name.orEmpty(),
            rate = editingCustomer?.displayRate?:"",
            position = editingCustomer?.sortOrder?.toString().orEmpty(),
            advanceAmount = editingCustomer?.displayAdvanceAmount?.toString().orEmpty(),
            isEdit = editingCustomer != null
        )
    )
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CustomerFormUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // -------------------------
    // INPUT HANDLERS
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
    // SAVE
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

        when {
            position == null -> {
                _uiState.update { it.copy(positionError = "Invalid position") }
                valid = false
            }
            existingCustomers.any {
                it.sortOrder == position && it.id != editingCustomer?.id
            } -> {
                _uiState.update {
                    it.copy(positionError = "Sort order already exists")
                }
                valid = false
            }
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
            repository.upsertCustomer(customer)
            _uiEvent.emit(CustomerFormUiEvent.Dismiss)
        }
    }
}
