package com.miassolutions.milkledger.presentation.customerandsales.sales.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class BalanceHistoryItem(
    val date: LocalDate,
    val change: Double,        // +credit / -debit
    val balanceAfter: Double,
    val note: String?
) : Parcelable

