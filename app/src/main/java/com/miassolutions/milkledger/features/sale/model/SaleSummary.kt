package com.miassolutions.milkledger.features.sale.model

data class SaleSummary(
    val totalAmount: Long = 0,
    val grossVolume: Double = 0.0,
    val totalDeduction: Double = 0.0,
    val netVolume: Double = 0.0,
    val avgRate: Double = 0.0,
    val totalReceived: Long = 0
)
