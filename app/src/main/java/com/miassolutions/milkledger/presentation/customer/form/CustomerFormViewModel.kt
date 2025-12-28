package com.miassolutions.milkledger.presentation.customer.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.util.CustomerSaveError
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.presentation.customer.customerdetail.CustomerUiEvent
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


    private val _uiEffect = MutableSharedFlow<CustomerFormUiEffect>()
    val uiEffect: SharedFlow<CustomerFormUiEffect> = _uiEffect.asSharedFlow()


    fun onEvent(event: CustomerFormUiEvent) {
        when (event) {
            is CustomerFormUiEvent.OnPositionChanged -> {
                updateState { it.copy(position = event.value, positionError = null) }
            }

            is CustomerFormUiEvent.OnNameChanged -> {
                updateState { it.copy(name = event.value, nameError = null) }
            }

            is CustomerFormUiEvent.OnRateChanged -> {
                updateState { it.copy(rate = event.value, rateError = null) }
            }

            is CustomerFormUiEvent.OnAdvanceAmountChanged -> {
                updateState { it.copy(advanceAmount = event.value) }
            }

            CustomerFormUiEvent.OnSaveClicked -> {
                onSaveClicked()
            }
        }
    }

    private fun updateState(reducer: (CustomerFormUiState) -> CustomerFormUiState) {
        _uiState.update(reducer)
    }


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
                            isEdit = true
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
    // Save
    // -------------------------
    private fun onSaveClicked() {
        val state = _uiState.value

        val name = state.name.trim()
        val rate = state.rate.toDoubleOrNull()
        val position = state.position.toIntOrNull()

        val nameError = if (name.isBlank()) "Name is required" else null
        val rateError = if (rate == null) "Rate is required" else null
        val positionError = if (position == null) "Positions is required" else null

        val hasError = nameError != null ||
                rateError != null ||
                positionError != null

        if (hasError) {
            updateState {
                it.copy(
                    nameError = nameError,
                    rateError = rateError,
                    positionError = positionError,
                    isSaving = false
                )
            }
            return
        }


        val customer = Customer(
            id = customerId ?: UUID.randomUUID().toString(),
            name = name,
            rate = rate ?: 0.0,
            sortOrder = position ?: 0,
            advanceAmount = state.advanceAmount.toDoubleOrNull() ?: 0.0,
            isDefault = true
        )

        viewModelScope.launch {

            updateState {
                it.copy(
                    isSaving = true,
                    nameError = null,
                    positionError = null,
                    rateError = null
                )
            }

            try {
                repository.upsertCustomer(customer)
                _uiEffect.emit(CustomerFormUiEffect.Dismiss)

            } catch (e: CustomerSaveError.SortOrderAlreadyExists) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        positionError = "Position ${e.sortOrder} already exists"
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isSaving = false,
                    )
                }
            }
        }
    }
}
