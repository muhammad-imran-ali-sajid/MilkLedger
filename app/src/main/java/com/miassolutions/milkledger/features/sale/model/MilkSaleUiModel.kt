package com.miassolutions.milkledger.features.sale.model

data class MilkSaleUiModel(
    val id: String,
    val dateMillis: Long,
    val customerId: String,
    val customerName: String,

    // Milk Details
    val quantity: Double,
    val deduction: Double = 0.0,
    val netQuantity: Double,
    val totalAmount: Long,

    val rate: Double,

    val paymentReceived: Long = 0,
    val currentBalance: Long = 0,

    val note: String?
)