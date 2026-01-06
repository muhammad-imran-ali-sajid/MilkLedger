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

    // Note: Payment aur Balance fetch krna complex query hoti hai list k liye,
    // filhal hum 0 show karenge ya alag logic se layenge.
    val paymentReceived: Long = 0,
    val currentBalance: Long = 0,

    val note: String?
)