package com.miassolutions.milkledger.features.purchase.model

data class PurchaseSummary(
    val totalAmount: Long = 0L,
    val totalVolume: Double = 0.0,

    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,

    val totalTs: Double = 0.0,
    val qualityVolume: Double = 0.0,

    val avgRate: Double = 0.0,
    val totalPaid: Long = 0L
)
