package com.miassolutions.milkledger.features.purchase.ui.balancehistory


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BalanceHistoryViewModel @Inject constructor(
    private val ledgerDao: LedgerDao
) : ViewModel() {

    // 🔥 Type Change: Ab hum UiModel return karenge
    private val _historyFlow = MutableStateFlow<List<DailyLedgerUiModel>>(emptyList())
    val historyFlow = _historyFlow.asStateFlow()

    private val _balanceFlow = MutableStateFlow<Long>(0)
    val balanceFlow = _balanceFlow.asStateFlow()

    fun loadHistory(accountId: String) {
        viewModelScope.launch {
            // 1. Get History List & Group by Date
            ledgerDao.getLedgerHistory(accountId).collectLatest { entities ->

                // Grouping Logic
                val groupedList = entities
                    .groupBy { it.dateMillis.toLocalDate() } // Same Date walay ikhatay
                    .map { (date, dailyEntries) ->

                        // Calculate Totals for that day
                        val totalDebit = dailyEntries.sumOf { it.debit }
                        val totalCredit = dailyEntries.sumOf { it.credit }

                        // Descriptions combine karein (Optional)
                        val types = dailyEntries.map {
                            when(it.type) {
                                LedgerEntryType.MILK_SALE -> "Sale"
                                LedgerEntryType.MILK_PURCHASE -> "Purchase"
                                LedgerEntryType.CASH_RECEIVED -> "Cash In"
                                LedgerEntryType.CASH_PAID -> "Cash Out"
                                else -> "Trx"
                            }
                        }.distinct().joinToString(" & ")

                        DailyLedgerUiModel(
                            dateMillis = date.toMillis(),
                            totalDebit = totalDebit,
                            totalCredit = totalCredit,
                            description = types
                        )
                    }
                    .sortedByDescending { it.dateMillis } // Newest first

                _historyFlow.value = groupedList
            }
        }

        viewModelScope.launch {
            ledgerDao.getAccountBalance(accountId).collectLatest {
                _balanceFlow.value = it
            }
        }
    }
}