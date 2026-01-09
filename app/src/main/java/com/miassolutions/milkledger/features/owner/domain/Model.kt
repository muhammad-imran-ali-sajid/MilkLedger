package com.miassolutions.milkledger.features.owner.domain


data class OwnerDashboardData(
    val netProfit: Long,
    val totalDrawings: Long,
    val transactions: List<OwnerTransactionUiModel>
) {
    // Computed Property
    val retainedEarnings: Long
        get() = netProfit - totalDrawings
}

data class OwnerTransactionUiModel(
    val id: String,
    val dateMillis: Long,
    val amount: Long,
    val note: String,
    val isPersonalExpense: Boolean // True = 🛍️, False = 💵
)