package com.miassolutions.milkledger.features.purchase.ui.balancehistory

data class DailyLedgerUiModel(
    val dateMillis: Long,
    val totalDebit: Long,
    val totalCredit: Long,
    val description: String // Example: "Milk Sale + Payment"
)