package com.miassolutions.milkledger.features.common


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
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

    private val _historyFlow = MutableStateFlow<List<FinancialLedgerEntity>>(emptyList())
    val historyFlow = _historyFlow.asStateFlow()

    private val _balanceFlow = MutableStateFlow<Long>(0)
    val balanceFlow = _balanceFlow.asStateFlow()

    // 🔥 Updated Function: Accepts optional dateLimit
    fun loadHistory(accountId: String, dateLimit: Long?) {

        // Agar dateLimit null hai (matlab user ne simple balance click kia), to aaj ki date le lo
        val targetDate = dateLimit ?: System.currentTimeMillis()

        viewModelScope.launch {
            // 1. Get History (Up to target date)
            ledgerDao.getLedgerHistoryUntil(accountId, targetDate).collectLatest {
                _historyFlow.value = it
            }
        }

        viewModelScope.launch {
            // 2. Get Balance (As of target date)
            ledgerDao.getAccountBalanceUntil(accountId, targetDate).collectLatest {
                _balanceFlow.value = it
            }
        }
    }
}