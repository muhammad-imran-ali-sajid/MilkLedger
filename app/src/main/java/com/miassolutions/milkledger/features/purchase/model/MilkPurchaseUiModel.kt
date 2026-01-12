package com.miassolutions.milkledger.features.purchase.model


import com.miassolutions.milkledger.utils.extensions.toLocalDate
import java.time.LocalDate

data class MilkPurchaseUiModel(
    val id: String,
    val dateMillis: Long,
    val supplierId: String,
    val supplierName: String,

    // Purchase Specific Fields
    val volume: Double,
    val fat: Double,
    val lr: Double,
    val ts: Double, // Calculated

    val rate: Double,
    val totalAmount: Long,

    val previousRate: Double? = null,

    val paymentDateMillis: Long?,
    val paymentMade: Long = 0, // Amount Paid
    val currentBalance: Long = 0,

    val note: String?
) {
    val paymentDate: LocalDate?
        get() = paymentDateMillis?.toLocalDate()
}