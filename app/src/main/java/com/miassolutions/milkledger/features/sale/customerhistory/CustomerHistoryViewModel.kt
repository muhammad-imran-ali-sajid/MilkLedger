package com.miassolutions.milkledger.features.sale.customerhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
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
) : BaseViewModel<CustomerHistoryUiState, CustomerHistoryUiEvent, CustomerHistoryUiEffect>(CustomerHistoryUiState()) {

    private val customerId: String = savedStateHandle["customerId"] ?: ""
    private val customerName: String = savedStateHandle["customerName"] ?: ""

    private var historyJob: Job? = null

    init {
        // Init state with Customer Name
        updateState { it.copy(customerName = customerName) }

        if (customerId.isNotEmpty()) {
            // Default: Load All History
            loadHistory(0L, Long.MAX_VALUE)
            loadCurrentBalance()
        }
    }

    private fun loadHistory(start: Long, end: Long) {
        // Pichli job cancel karein (Agar user jaldi jaldi filter change kare)
        historyJob?.cancel()

        historyJob = repository.getCustomerHistory(customerId, start, end)
            .onEach { list ->
                val totalMilk = list.sumOf { it.netQuantity }
                val totalReceived = list.sumOf { it.paymentReceived }

                updateState {
                    it.copy(
                        isLoading = false,
                        transactions = list,
                        summaryMilk = totalMilk,
                        summaryReceived = totalReceived
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCurrentBalance() {
        // Balance hamesha 'Overall' hota hai, Date filter ka is par asar nahi hona chahiye
        repository.getCustomerBalance(customerId)
            .onEach { balance ->
                updateState { it.copy(currentTotalBalance = balance) }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CustomerHistoryUiEvent) {
        when(event) {
            // 1. Date Filter Changed
            is CustomerHistoryUiEvent.OnDateFilterChanged -> { // Make sure ye Event class me defined ho
                updateState { it.copy(dateRangeText = event.label) }
                loadHistory(event.start, event.end)
            }

            // 2. Click on Item (Edit)
            is CustomerHistoryUiEvent.OnTransactionClick -> {
                emitEffect(CustomerHistoryUiEffect.NavigateToEditSale(event.saleId))
            }

            // 3. Back Press
            CustomerHistoryUiEvent.OnBackClick -> {
                emitEffect(CustomerHistoryUiEffect.NavigateBack)
            }

            else -> {}
        }
    }
}
