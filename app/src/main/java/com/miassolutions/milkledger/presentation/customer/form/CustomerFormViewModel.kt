package com.miassolutions.milkledger.presentation.customer.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.util.CustomerSaveError
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.presentation.customer.model.CustomerUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CustomerFormViewModel @Inject constructor(
    private val repository: CustomerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val customerId: String? = savedStateHandle["customerId"]


    private val _uiState = MutableStateFlow(CustomerFormUiState())
    val uiState = _uiState.asStateFlow()


    private val _uiEvent = MutableSharedFlow<CustomerFormUiEvent>()
    val uiEvent: SharedFlow<CustomerFormUiEvent> = _uiEvent.asSharedFlow()


    private fun loadCustomer(customerId: String) {
        viewModelScope.launch {
            repository.getCustomerById(customerId)
                .filterNotNull()
                .first()
                .let { customer ->
                    _uiState.update {
                        it.copy(
                            name = customer.name,
                            rate = customer.rate.toString(),
                            position = customer.sortOrder.toString(),
                            advanceAmount = customer.advanceAmount.toString(),
                            isEdit = true,

                            )
                    }
                }


        }
    }


    //cache


    init {
        customerId?.let { loadCustomer(customerId) }
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
        }

        if (!valid) return

        val customer = Customer(
            id = customerId ?: UUID.randomUUID().toString(),
            name = state.name.trim(),
            rate = rate!!,
            sortOrder = position!!,
            advanceAmount = state.advanceAmount.toDoubleOrNull() ?: 0.0,
            isDefault = true
        )

        viewModelScope.launch {
            try {
                repository.upsertCustomer(customer)
                _uiEvent.emit(CustomerFormUiEvent.Dismiss)

            } catch (e: CustomerSaveError.SortOrderAlreadyExists) {
                _uiState.update {
                    it.copy(
                        positionError = "Position ${e.sortOrder} already exists"
                    )
                }
            }
        }
    }
}
