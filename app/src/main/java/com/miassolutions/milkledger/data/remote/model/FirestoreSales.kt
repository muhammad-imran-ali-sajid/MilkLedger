package com.miassolutions.milkledger.data.remote.model

data class FirestoreSales(
    // All properties MUST have a default value for Firestore compatibility
    val saleId: String = "",
    val customerId: String = "", // <-- FIXED: Added default value
    val date: String = "",
    val volume: Double = 0.0,
    val deduction: Double = 0.0,
    val netMilk: Double = 0.0,
    val price: Double = 0.0,
    val paid: Double = 0.0,
    val balance: Double = 0.0,
    val rateUsed: Double = 0.0,                    // snapshot of rate at entry time
    val notes: String? = null,

    val isSynced: Boolean = false,
    val updatedAt: String = "",
    val deletedAt: String? = null
)