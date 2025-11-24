package com.miassolutions.milkledger.data.remote.model

import java.time.LocalDateTime

data class FirestoreExpense(
    val id: String = "",
    val date: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val expenseNote: String = "",
    val default: Boolean? = null,


    val isSynced: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = "",
    val deletedAt: String? = null
)
