package com.miassolutions.milkledger.presentation.profit

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class ProfitListModel(
    val id : String,
    val date : LocalDate,
    val profit : Double,
    val profitReceived : Double,
    val balance : Double
): Parcelable
