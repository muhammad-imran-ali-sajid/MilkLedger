package com.miassolutions.milkledger.data.remote.model

import java.time.LocalDateTime

data class FirestoreProfit(

    val profitId: String = "",
    val receivedDate: String = "",
    val netProfit : Double = 0.0,
    val receivedProfit : Double = 0.0,
    val notes : String? = null,

    val createdAt : String = "",

    val isSynced: Boolean = false,
    val updatedAt: String = "",
    val deletedAt: LocalDateTime? = null

)

