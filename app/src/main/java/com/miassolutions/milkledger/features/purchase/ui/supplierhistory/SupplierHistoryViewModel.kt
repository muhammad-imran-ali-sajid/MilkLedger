package com.miassolutions.milkledger.features.purchase.ui.supplierhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.features.purchase.model.PurchaseSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SupplierHistoryViewModel @Inject constructor(
    private val repository: MilkPurchaseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<SupplierHistoryUiState, SupplierHistoryUiEvent, SupplierHistoryUiEffect>(
    SupplierHistoryUiState()
) {

    private val supplierId: String = savedStateHandle["supplierId"] ?: ""
    private val supplierName: String = savedStateHandle["supplierName"] ?: ""

    // 🔥 Jobs to handle cancellation (Agar user jaldi jaldi date change kare)
    private var historyJob: Job? = null
    private var summaryJob: Job? = null

    init {
        updateState { it.copy(supplierName = supplierName) }

        // 🔥 IMPORTANT: Start mein data load zaroor call karein
        if (supplierId.isNotEmpty()) {
            loadData(0L, Long.MAX_VALUE)
        }
    }

    // Function ka naam 'loadHistory' se change kr k 'loadData' kr dia (List + Summary)
    private fun loadData(start: Long, end: Long) {

        // 1. Loading Start
        updateState { it.copy(isLoading = true) }

        // ----------------------------------------
        // JOB 1: Load Transaction List
        // ----------------------------------------
        historyJob?.cancel()
        historyJob = repository.getSupplierHistory(supplierId, start, end)
            .onEach { list ->
                updateState {
                    it.copy(
                        isLoading = false,
                        transactions = list, // 🔥 Save original list
                        displayedTransactions = filterTransactions(list, it.searchQuery) // 🔥 Apply filter
                    )
                }
            }
            .launchIn(viewModelScope)

        // ----------------------------------------
        // JOB 2: Load Summary Stats (🔥 NEW ADDITION)
        // ----------------------------------------
        summaryJob?.cancel()
        summaryJob = repository.getSupplierSummary(supplierId, start, end)
            .onEach { stats ->
                // UI State ki 'summary' field update karein
                updateState {
                    it.copy(summary = stats)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: SupplierHistoryUiEvent) {
        when (event) {
            is SupplierHistoryUiEvent.OnDateFilterChanged -> {
                updateState { it.copy(dateRangeText = event.label) }
                // Date change hone par dono data reload honge
                loadData(event.start, event.end)
            }
            
            is SupplierHistoryUiEvent.OnSearchQueryChanged -> {
                val query = event.query
                updateState { state ->
                    state.copy(
                        searchQuery = query,
                        displayedTransactions = filterTransactions(state.transactions, query)
                    )
                }
            }

            SupplierHistoryUiEvent.OnBackClick -> emitEffect(SupplierHistoryUiEffect.NavigateBack)
        }
    }
    
    // 🔥 Filter Logic Function
    private fun filterTransactions(list: List<MilkPurchaseUiModel>, query: String): List<MilkPurchaseUiModel> {
        if (query.isBlank()) return list
        
        return list.filter { item ->
            // Note: In variables ko apne MilkPurchaseUiModel ki properties k hisaab se theek kar lein
            val volumeStr = item.volume.toString()
            val totalAmountStr = item.totalAmount.toString()
            val paymentStr = item.paymentMade?.toString() ?: "" // Agar amountPaid ki property hy
            
            volumeStr.contains(query, ignoreCase = true) ||
                    totalAmountStr.contains(query, ignoreCase = true) ||
                    paymentStr.contains(query, ignoreCase = true)
        }
    }
}