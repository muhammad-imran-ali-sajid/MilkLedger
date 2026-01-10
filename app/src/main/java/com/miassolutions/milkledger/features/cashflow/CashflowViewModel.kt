package com.miassolutions.milkledger.features.cashflow

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CashflowViewModel @Inject constructor(
    private val repository: CashflowRepository
) : BaseViewModel<CashflowUiState, CashflowUiEvent, CashflowUiEffect>(CashflowUiState()) {

    init {
        // Default: Load Today/This Month (handled by UI Filter callback)
    }

    override fun onEvent(event: CashflowUiEvent) {
        when(event) {
            is CashflowUiEvent.OnDateFilterChanged -> {
                loadData(event.start, event.end)
            }
        }
    }

    private fun loadData(start: Long, end: Long) {
        updateState { it.copy(isLoading = true) }

        // Combine Summary & List Flows
        combine(
            repository.getCashflowSummary(start, end),
            repository.getTransactions(start, end)
        ) { summary, list ->
            CashflowUiState(
                isLoading = false,
                totalIn = summary.totalIn,
                totalOut = summary.totalOut,
                netCash = summary.totalIn - summary.totalOut, // Formula
                transactions = list
            )
        }.onEach { newState ->
            updateState { newState }
        }.launchIn(viewModelScope)
    }
}

// --- State Class ---
data class CashflowUiState(
    val isLoading: Boolean = false,
    val totalIn: Long = 0,
    val totalOut: Long = 0,
    val netCash: Long = 0,
    val transactions: List<FinancialLedgerEntity> = emptyList()
)

sealed class CashflowUiEvent {
    data class OnDateFilterChanged(val start: Long, val end: Long) : CashflowUiEvent()
}

sealed interface CashflowUiEffect {

}