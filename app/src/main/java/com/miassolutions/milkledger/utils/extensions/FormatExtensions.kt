package com.miassolutions.milkledger.utils.extensions


import android.widget.TextView
import androidx.core.content.ContextCompat
import com.miassolutions.milkledger.R
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.roundToLong

fun Long.toSignedBalance(): String {
    val rupees = this / 100.0

    // Rounded absolute value
    val rounded = round(abs(rupees)).toLong()

    // Zero case — sab se pehle handle karo
    if (rounded == 0L) return "0"

    return when {
        this > 0 -> "+$rounded"
        else -> "-$rounded"
    }
}




fun TextView.setBalanceColorWithRoundRupee(amountPaisa: Long, prefix: String = "") {

    // Calculate Rupees (Rounded)
    val rupees = (amountPaisa / 100.0).roundToLong()

    // Set Text with Commas (e.g. Rs. 5,000)
    text = "%,d".format(rupees)

    // Set Color based on Positive/Negative
    if (amountPaisa >= 0) {
        setTextColor(ContextCompat.getColor(context, R.color.green_700)) // Ya jo apka color ho
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.red))
    }
}

fun TextView.setBalanceWithColor(amountPaisa: Long) {

    // Calculate Rupees (Rounded)
    val rupees = (amountPaisa / 100.0).roundToLong()

    // Set Text with Commas (e.g. Rs. 5,000)
    text = "%,d".format(rupees)

    // Set Color based on Positive/Negative
    if (amountPaisa >= 0) {
        setTextColor(ContextCompat.getColor(context, R.color.milk_profit_color)) // Ya jo apka color ho
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.milk_expense_color))
    }
}