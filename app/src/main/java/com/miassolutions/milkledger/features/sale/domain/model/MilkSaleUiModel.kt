package com.miassolutions.milkledger.features.sale.domain.model


data class MilkSaleUiModel(
    val id: String,
    val dateMillis: Long,
    val customerId: String,
    val customerName: String,

    // Milk Details
    val quantity: Double,
    val deduction: Double = 0.0, // Deduction entity me add krna prega ya logic lagani pregi
    val netQuantity: Double,
    val totalAmount: Long, // Price (Paisa)


    val paymentReceived: Long = 0,
    val currentBalance: Long = 0,

    val note: String?
)