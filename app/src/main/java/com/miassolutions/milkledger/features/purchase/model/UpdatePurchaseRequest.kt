package com.miassolutions.milkledger.features.purchase.model

import java.time.LocalDate

data class UpdatePurchaseRequest(
    val purchaseId: String,
    val supplierId: String,
    val date: LocalDate,
    val paymentDate: LocalDate?,
    val volume: Double,
    val fat: Double,
    val lr: Double,
    val rate: Double,
    val amountPaid: Long,
    val note: String?
)