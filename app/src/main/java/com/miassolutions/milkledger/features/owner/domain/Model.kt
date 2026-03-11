package com.miassolutions.milkledger.features.owner.domain


data class OwnerDashboardData(
    val netProfit: Long,
    val totalDrawings: Long,
    val retainedEarnings: Long,
    val transactions: List<OwnerTransactionUiModel>
)

data class OwnerTransactionUiModel(
    val id: String,
    val dateMillis: Long,
    val amount: Long,
    val note: String,
    val isPersonalExpense: Boolean // True = 🛍️, False = 💵
)