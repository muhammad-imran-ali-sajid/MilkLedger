package com.miassolutions.milkledger.data.remote.model

import java.time.LocalDate
import java.util.UUID

data class FirestorePurchase(
    val purchaseId: String = "",
    val supplierId: String = "",
    val date: String = "",
    val milkAmount: Double = 0.0,
    val fat: Double = 0.0,
    val lr: Double = 0.0,
    val ts: Double = 0.0,
    val milkPrice: Double = 0.0,
    val payment: Double = 0.0,
    val balance: Double = 0.0,
    val rateUsed: Double = 0.0,
    val notes: String? = null,

    val isSynced: Boolean = false,
    val updatedAt: String = "",
    val deletedAt: String? = null

)
