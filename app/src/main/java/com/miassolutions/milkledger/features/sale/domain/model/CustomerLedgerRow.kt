package com.miassolutions.milkledger.features.sale.domain.model

data class CustomerLedgerRow(
    val dateMillis: Long,
    val credit: Double,
    val debit: Double,
    val profitImpact: Double,
    val note: String?
)

