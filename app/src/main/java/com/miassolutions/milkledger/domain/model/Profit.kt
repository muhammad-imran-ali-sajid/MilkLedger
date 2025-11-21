package com.miassolutions.milkledger.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
@Parcelize
data class Profit(
    val profitId: String,
    val receivedDate: LocalDate = LocalDate.now(),
    val netProfit : Double = 0.0,
    val receivedProfit : Double = 0.0,
    val notes : String? = null,

    val isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: LocalDateTime? = null
): Parcelable