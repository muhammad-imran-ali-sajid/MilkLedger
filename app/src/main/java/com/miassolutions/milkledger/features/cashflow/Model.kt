package com.miassolutions.milkledger.features.cashflow

data class CashflowSummary(
    val totalIn: Long,  // Sum of Credit
    val totalOut: Long  // Sum of Debit
)