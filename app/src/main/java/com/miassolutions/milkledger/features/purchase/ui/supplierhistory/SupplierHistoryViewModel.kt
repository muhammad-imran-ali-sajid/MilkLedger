package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SupplierHistoryViewModel @Inject constructor(
    private val repository: MilkPurchaseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<SupplierHistoryUiState, SupplierHistoryUiEvent, SupplierHistoryUiEffect>(SupplierHistoryUiState()) {

    private val supplierId: String = savedStateHandle["supplierId"] ?: ""
    private val supplierName: String = savedStateHandle["supplierName"] ?: ""

    private var historyJob: Job? = null

    init {
        updateState { it.copy(supplierName = supplierName) }
        if (supplierId.isNotEmpty()) {
            loadHistory(0L, Long.MAX_VALUE) // Default All
            loadCurrentBalance()
        }
    }

    private fun loadHistory(start: Long, end: Long) {
        historyJob?.cancel()
        historyJob = repository.getSupplierHistory(supplierId, start, end)
            .onEach { list ->
                // Summary Calculation
                val totalVol = list.sumOf { it.volume }
                val totalAmount = list.sumOf { it.totalAmount }
                val totalPaid = list.sumOf { it.paymentMade }

                updateState {
                    it.copy(
                        isLoading = false,
                        transactions = list,
                        summaryMilk = totalVol,
                        summaryAmount = totalAmount,
                        summaryPaid = totalPaid
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCurrentBalance() {
        // Current Balance hamesha overall hota hai (Filter se effect nahi hota)
//        repository.getCustomerBalance(supplierId) // Reuse Account Balance Query
//            .onEach { balance ->
//                updateState { it.copy(currentTotalBalance = balance) }
//            }
//            .launchIn(viewModelScope)
    }

    override fun onEvent(event: SupplierHistoryUiEvent) {
        when(event) {
            is SupplierHistoryUiEvent.OnDateFilterChanged -> {
                updateState { it.copy(dateRangeText = event.label, startDate = event.start, endDate = event.end) }
                loadHistory(event.start, event.end)
            }
            is SupplierHistoryUiEvent.OnTransactionClick -> {
                emitEffect(SupplierHistoryUiEffect.NavigateToEditPurchase(event.purchaseId))
            }
            SupplierHistoryUiEvent.OnBackClick -> emitEffect(SupplierHistoryUiEffect.NavigateBack)
        }
    }
}