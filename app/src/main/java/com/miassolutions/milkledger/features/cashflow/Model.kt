package com.miassolutions.milkledger.features.cashflow

import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity

data class CashflowSummary(
    val totalIn: Long,  // Sum of Credit
    val totalOut: Long  // Sum of Debit
)


sealed interface LedgerListItem {

    // 1. Header (e.g., "Milk Sales", "Expenses")
    data class Header(val title: String) : LedgerListItem {
        override val id = title // DiffUtil k liye unique ID
    }

    // 2. Transaction Item (Asal Data)
    data class Transaction(val data: FinancialLedgerEntity) : LedgerListItem {
        override val id = data.ledgerId
    }

    // Helper for DiffUtil
    val id: String
}