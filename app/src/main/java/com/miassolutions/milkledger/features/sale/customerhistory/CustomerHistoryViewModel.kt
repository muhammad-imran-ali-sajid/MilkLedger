package com.miassolutions.milkledger.features.sale.customerhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.model.SaleSummary
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
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
                updateState { state ->
                    state.copy(
                        isLoading = false,
                        transactions = list,
                        displayedTransactions = filterTransactions(list, state.searchQuery)
                    )
                }
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
            is CustomerHistoryUiEvent.OnSearchQueryChanged -> {
                val query = event.query
                updateState { state ->
                    state.copy(
                        searchQuery = query,
                        displayedTransactions = filterTransactions(state.transactions, query)
                    )
                }
            }

            // 3. Back Press
            CustomerHistoryUiEvent.OnBackClick -> {
                emitEffect(CustomerHistoryUiEffect.NavigateBack)
            }

        }
    }
    
    private fun filterTransactions(list: List<MilkSaleUiModel>, query: String): List<MilkSaleUiModel> {
        if (query.isBlank()) return list
        
        return list.filter { item ->
            // Apne model ki properties ke hisaab se condition lagayen.
            // .toString() isliye takay numbers bhi text ki tarah match ho jayen (e.g. "140" type karne pe mil jaye)
            val volumeStr = item.quantity.toString()
            val totalAmountStr = item.totalAmount.toString()
            val paymentStr = item.paymentReceived?.toString() ?: ""
            
            // Agar query inme se kisi bhi cheez me match hoti hai to item list me rahega
            volumeStr.contains(query, ignoreCase = true) ||
                    totalAmountStr.contains(query, ignoreCase = true) ||
                    paymentStr.contains(query, ignoreCase = true)
        }
    }
}
