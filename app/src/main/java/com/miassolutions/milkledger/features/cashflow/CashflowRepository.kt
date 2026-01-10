package com.miassolutions.milkledger.features.cashflow

import com.miassolutions.milkledger.core.localdb.ledger.LedgerDao
import javax.inject.Inject

class CashflowRepository @Inject constructor(
    private val ledgerDao: LedgerDao
) {
    fun getCashflowSummary(start: Long, end: Long) = ledgerDao.getCashflowSummary(start, end)

    fun getTransactions(start: Long, end: Long) = ledgerDao.getLedgerEntriesInRange(start, end)
}