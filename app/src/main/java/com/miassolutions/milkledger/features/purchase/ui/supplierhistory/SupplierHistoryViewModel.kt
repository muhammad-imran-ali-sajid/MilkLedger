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
) : BaseViewModel<SupplierHistoryUiState, SupplierHistoryUiEvent, SupplierHistoryUiEffect>(
    SupplierHistoryUiState()
) {

    private val supplierId: String = savedStateHandle["supplierId"] ?: ""
    private val supplierName: String = savedStateHandle["supplierName"] ?: ""

    private var historyJob: Job? = null

    init {
        updateState { it.copy(supplierName = supplierName) }

    }

    private fun loadHistory(start: Long, end: Long) {
        historyJob?.cancel()
        historyJob = repository.getSupplierHistory(supplierId, start, end)
            .onEach { list ->

                val totalVol = list.sumOf { it.volume }
                val totalAmount = list.sumOf { it.totalAmount }
                val totalPaid = list.sumOf { it.paymentMade }

                updateState {
                    it.copy(
                        isLoading = false,
                        transactions = list,
                    )
                }
            }
            .launchIn(viewModelScope)
    }


    override fun onEvent(event: SupplierHistoryUiEvent) {
        when (event) {
            is SupplierHistoryUiEvent.OnDateFilterChanged -> {
                updateState { it.copy(dateRangeText = event.label) }
                loadHistory(event.start, event.end)
            }

            SupplierHistoryUiEvent.OnBackClick -> emitEffect(SupplierHistoryUiEffect.NavigateBack)
        }
    }
}