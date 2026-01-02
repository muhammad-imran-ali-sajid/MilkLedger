package com.miassolutions.milkledger.features.sale.ui.saleform

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.customer.ui.mapper.toDropDownUi
import com.miassolutions.milkledger.features.customer.ui.model.DropDownCustomerListUi
import com.miassolutions.milkledger.features.sale.domain.usecase.CalculateSaleUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.CheckDuplicateSaleUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.ObserveCustomerUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.SaveSaleUseCase
import com.miassolutions.milkledger.features.sale.mapper.toDomain

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleFormViewModel @Inject constructor(
    observeCustomer: ObserveCustomerUseCase,
    private val checkDuplicateSale: CheckDuplicateSaleUseCase,
    private val saveSale: SaveSaleUseCase,
    private val calculateSale: CalculateSaleUseCase

) : BaseViewModel<SaleFormUiState, SaleFormUiEvent, SaleFormUiEffect>(initialState = SaleFormUiState()) {

    init {
        observeCustomer()
            .map { list -> list.map { it.toDropDownUi() } }
            .onEach { customers ->
                updateState { it.copy(customers = customers) }
            }
            .launchIn(viewModelScope)

        uiState.map {
            calculateSale(
                volume = it.volume,
                deduction = it.deduction,
                rate = it.rateUsed,
                received = it.receivedAmount
            )
        }
            .onEach { result ->
                updateState {
                    it.copy(
                        netMilk = result.netMilk,
                        price = result.price,
                        balance = result.balance
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: SaleFormUiEvent) {
        when (event) {
            is SaleFormUiEvent.CustomerSelected -> {
                updateState {
                    it.copy(
                        selectedCustomer = DropDownCustomerListUi(
                            id = event.customerId,
                            name = event.customerName,
                            rate = event.rate
                        ),
                        rateUsed = event.rate
                    )
                }


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
                save(closeAfter = false)
            }

            SaleFormUiEvent.SaveClicked -> {
                save(closeAfter = true)
            }

            is SaleFormUiEvent.SaleDateSelected -> {
                updateState { it.copy(saleDate = event.date) }
            }

            is SaleFormUiEvent.ReceivedDateSelected -> {
                updateState { it.copy(receivedDate = event.date) }
            }

        }
    }

    private fun save(closeAfter: Boolean) = viewModelScope.launch {
        val state = currentState
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


        val isTrue = state.volume.isBlank() && state.receivedAmount.isBlank()

        if (isTrue) {
            emitEffect(SaleFormUiEffect.ShowToast("Enter volume or amount"))
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




}