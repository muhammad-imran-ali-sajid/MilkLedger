package com.miassolutions.milkledger.features.sale.customerhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.model.SaleSummary
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CustomerHistoryViewModel @Inject constructor(
    private val repository: MilkSaleRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<CustomerHistoryUiState, CustomerHistoryUiEvent, CustomerHistoryUiEffect>(
    CustomerHistoryUiState()
) {

    private val customerId: String = savedStateHandle["customerId"] ?: ""
    private val customerName: String = savedStateHandle["customerName"] ?: ""

    private var historyJob: Job? = null

    init {

        updateState { it.copy(customerName = customerName) }
    }

    private fun loadHistory(start: Long, end: Long) {
        // Pichli job cancel karein (Agar user jaldi jaldi filter change kare)
        historyJob?.cancel()

        historyJob = repository.getCustomerHistory(customerId, start, end)
            .onEach { list ->
                val grossMilk = list.sumOf { it.quantity }
                val totalDeduc = list.sumOf { it.deduction }
                val netMilk = list.sumOf { it.netQuantity }
                val totalAmount = list.sumOf { it.totalAmount }
                val totalReceived = list.sumOf { it.paymentReceived }
                val avgRate = if (netMilk > 0.0) {
                    totalAmount.toDouble() / netMilk
                } else {
                    0.0
                }

                updateState {
                    it.copy(
                        summary = SaleSummary(
                            totalAmount = totalAmount,
                            grossVolume = grossMilk,
                            totalDeduction = totalDeduc,
                            netVolume = netMilk,
                            avgRate = avgRate.toDouble(),
                            totalReceived = totalReceived
                        )
                    )
                }

                updateState { it.copy(isLoading = false, transactions = list) }
            }
            .launchIn(viewModelScope)
    }


    override fun onEvent(event: CustomerHistoryUiEvent) {
        when (event) {
            // 1. Date Filter Changed
            is CustomerHistoryUiEvent.OnDateFilterChanged -> { // Make sure ye Event class me defined ho
                updateState { it.copy(dateRangeText = event.label) }
                loadHistory(event.start, event.end)
            }


            // 3. Back Press
            CustomerHistoryUiEvent.OnBackClick -> {
                emitEffect(CustomerHistoryUiEffect.NavigateBack)
            }

        }
    }
}
