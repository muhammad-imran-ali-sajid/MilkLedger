package com.miassolutions.milkledger.presentation.profit

data class ProfitListModel(
    val id : String,
    val date : String,
    val profit : Double,
    val profitReceived : Double,
    val balance : Double
)
