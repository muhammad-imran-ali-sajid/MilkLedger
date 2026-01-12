package com.miassolutions.milkledger.features.cashflow

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
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

            // 🔥 Transformation Here: Raw List -> Grouped List with Headers
            val groupedTransactions = transformToGroupedList(list)

            CashflowUiState(
                isLoading = false,
                totalIn = summary.totalIn,
                totalOut = summary.totalOut,
                netCash = summary.totalIn - summary.totalOut,
                transactions = groupedTransactions // ✅ Pass Grouped List
            )
        }.onEach { newState ->
            updateState { newState }
        }.launchIn(viewModelScope)
    }

    // 🔥 Helper Function to Group Data
    private fun transformToGroupedList(rawData: List<FinancialLedgerEntity>): List<LedgerListItem> {
        val groupedList = mutableListOf<LedgerListItem>()

        // 1. Received (Cash In)
        val received = rawData.filter { it.type == LedgerEntryType.CASH_RECEIVED }
        if (received.isNotEmpty()) {
            groupedList.add(LedgerListItem.Header("Cash Received (In)"))
            groupedList.addAll(received.map { LedgerListItem.Transaction(it) })
        }

        // 2. Paid (Cash Out)
        val paid = rawData.filter { it.type == LedgerEntryType.CASH_PAID }
        if (paid.isNotEmpty()) {
            groupedList.add(LedgerListItem.Header("Payments (Out)"))
            groupedList.addAll(paid.map { LedgerListItem.Transaction(it) })
        }

        // 3. Expenses
        val expenses = rawData.filter { it.type == LedgerEntryType.BUSINESS_EXPENSE }
        if (expenses.isNotEmpty()) {
            groupedList.add(LedgerListItem.Header("Business Expenses"))
            groupedList.addAll(expenses.map { LedgerListItem.Transaction(it) })
        }

        // 4. Owner Drawings
        val drawings = rawData.filter { it.type == LedgerEntryType.OWNER_DRAWING }
        if (drawings.isNotEmpty()) {
            groupedList.add(LedgerListItem.Header("Owner Drawings / Personal"))
            groupedList.addAll(drawings.map { LedgerListItem.Transaction(it) })
        }

        // Agar list bilkul khali hai, to aap yahan empty state handle kar sakte hain
        // ya sirf empty list return kar den.
        return groupedList
    }
}

// --- Updated State Class ---
data class CashflowUiState(
    val isLoading: Boolean = false,
    val totalIn: Long = 0,
    val totalOut: Long = 0,
    val netCash: Long = 0,

    // 🔥 Changed from List<Entity> to List<LedgerListItem>
    val transactions: List<LedgerListItem> = emptyList()
)

sealed class CashflowUiEvent {
    data class OnDateFilterChanged(val start: Long, val end: Long) : CashflowUiEvent()
}

sealed interface CashflowUiEffect {
    // Add effects later if needed (e.g. Navigation)
}