package com.miassolutions.milkledger.presentation.customerandsales.sales.addsale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.presentation.customerandsales.customer.mapper.toDropDownUi
import com.miassolutions.milkledger.presentation.customerandsales.customer.mapper.toUI
import com.miassolutions.milkledger.presentation.customerandsales.customer.model.CustomerUi
import com.miassolutions.milkledger.presentation.customerandsales.customer.model.DropDownCustomerListUi
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.mapper.toDomain
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.state.SaleFormUiEffect
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.state.SaleFormUiEvent
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.state.SaleFormUiState
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.usecases.CalculateSaleUseCase
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.usecases.CheckDuplicateSaleUseCase
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.usecases.ObserveCustomerUseCase
import com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.usecases.SaveSaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleFormViewModel @Inject constructor(
    observeCustomer: ObserveCustomerUseCase,
    private val checkDuplicateSale: CheckDuplicateSaleUseCase,
    private val saveSale: SaveSaleUseCase,
    private val calculateSale: CalculateSaleUseCase

) : ViewModel() {

    private val _uiState = MutableStateFlow(SaleFormUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SaleFormUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun updateState(reducer: (SaleFormUiState) -> SaleFormUiState) {
        _uiState.update(reducer)
    }

    init {
        observeCustomer()
            .map { list -> list.map { it.toDropDownUi() } }
            .onEach { customers ->
                updateState { it.copy(customers = customers) }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SaleFormUiEvent) {
        when (event) {
            is SaleFormUiEvent.CustomerSelected -> {
                updateState {
                    it.copy(
                        selectedCustomer = DropDownCustomerListUi(
                            id = event.customerId,
                            name = event.customerName,
                            rate = event.rate
                        ),
                        rate = event.rate
                    )
                }
                recalculate()

            }

            is SaleFormUiEvent.VolumeChanged -> {
                updateState { it.copy(volume = event.value) }
            }

            is SaleFormUiEvent.DeductionChanged -> {
                updateState { it.copy(deduction = event.value) }
            }

            is SaleFormUiEvent.PaymentChanged -> {
                updateState { it.copy(receivedAmount = event.value) }
            }

            is SaleFormUiEvent.NotesChanged -> {
                updateState { it.copy(notes = event.value) }
            }

            SaleFormUiEvent.SaleDateClicked -> {
                emitEffect(SaleFormUiEffect.OpenSaleDatePicker)
            }

            SaleFormUiEvent.ReceivedDateClicked -> {
                emitEffect(SaleFormUiEffect.OpenReceivedDatePicker)
            }

            SaleFormUiEvent.SaveAndNewClicked -> {
                save(closeAfter = true)
            }

            SaleFormUiEvent.SaveClicked -> {
                save(closeAfter = false)
            }
        }
    }

    private fun save(closeAfter: Boolean) = viewModelScope.launch {
        val state = _uiState.value
        val customer = state.selectedCustomer ?: run {
            emitEffect(SaleFormUiEffect.ShowToast("Select Customer"))
            return@launch
        }

        if (checkDuplicateSale(customer.id, state.saleDate)) {
            emitEffect(
                SaleFormUiEffect.ShowToast(
                    "${customer.name} already exists for ${state.saleDate}"
                )
            )
            return@launch
        }

        val sale = state.toDomain()

        saveSale(sale)

        emitEffect(SaleFormUiEffect.ShowToast("Sale saved"))

        if (closeAfter) {
            emitEffect(SaleFormUiEffect.NavigateBack)
        } else {
            emitEffect(SaleFormUiEffect.ResetForm)
        }

    }


    private fun emitEffect(effect: SaleFormUiEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }

    private fun recalculate() {
        val state = _uiState.value
        val result = calculateSale(
            volume = state.volume,
            deduction = state.deduction,
            rate = state.rate,
            received = state.receivedAmount
        )

        updateState {
            it.copy(
                netMilk = result.netMilk,
                price = result.price,
                balance = result.balance
            )
        }
    }
}