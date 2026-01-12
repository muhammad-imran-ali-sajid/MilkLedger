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

    fun loadHistory(accountId: String) {
        viewModelScope.launch {
            // 1. Get History List
            ledgerDao.getLedgerHistory(accountId).collectLatest {
                _historyFlow.value = it
            }
        }

        viewModelScope.launch {
            // 2. Get Current Balance
            ledgerDao.getAccountBalance(accountId).collectLatest {
                _balanceFlow.value = it
            }
        }
    }
}