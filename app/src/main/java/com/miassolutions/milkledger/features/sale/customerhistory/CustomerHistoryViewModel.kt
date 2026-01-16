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

    // 🔥 Jobs to handle cancellation (Agar user jaldi jaldi date change kare)
    private var historyJob: Job? = null
    private var summaryJob: Job? = null

    init {

        updateState { it.copy(customerName = customerName) }
        if (customerId.isNotEmpty()) {
            loadData(0L, Long.MAX_VALUE)
        }
    }

    private fun loadData(start: Long, end: Long) {

        updateState { it.copy(isLoading = true) }
        // Pichli job cancel karein (Agar user jaldi jaldi filter change kare)

        //job 1 load transactions
        historyJob?.cancel()

        historyJob = repository.getCustomerHistory(customerId, start, end)
            .onEach { list ->
                updateState { it.copy(isLoading = false, transactions = list) }
            }
            .launchIn(viewModelScope)

        //job 2 load summary
        summaryJob?.cancel()
        summaryJob = repository.getCustomerSummary(customerId, start, end)
            .onEach { summary ->
                updateState { it.copy(summary = summary) }
            }
            .launchIn(viewModelScope)


    }


    override fun onEvent(event: CustomerHistoryUiEvent) {
        when (event) {
            // 1. Date Filter Changed
            is CustomerHistoryUiEvent.OnDateFilterChanged -> { // Make sure ye Event class me defined ho
                updateState { it.copy(dateRangeText = event.label) }
                loadData(event.start, event.end)
            }


            // 3. Back Press
            CustomerHistoryUiEvent.OnBackClick -> {
                emitEffect(CustomerHistoryUiEffect.NavigateBack)
            }

        }
    }
}
