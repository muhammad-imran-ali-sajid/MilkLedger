package com.miassolutions.milkledger.utils.extensions


import android.widget.TextView
import androidx.core.content.ContextCompat
import com.miassolutions.milkledger.R
import kotlin.math.abs
import kotlin.math.roundToLong

fun Long.toSignedBalanceRupee(): String {
    val rupees = this / 100.0
    val absRupees = abs(rupees) // Minus sign hata dein, hum khud lagayenge

    // Decimal formatting (Zero hatao agar nahi hai)
    val formattedPrice = if (absRupees % 1.0 == 0.0) {
        String.format("%.0f", absRupees)
    } else {
        String.format("%.2f", absRupees)
    }

    return when {
        this > 0 -> "+ Rs. $formattedPrice" // Positive: Leny hen
        this < 0 -> "- Rs. $formattedPrice" // Negative: Deny hen
        else -> "Rs. 0"                     // Zero
    }
}

fun Long.toSignedBalance(): String {
    val rupees = this / 100.0
    val absRupees = abs(rupees) // Minus sign hata dein, hum khud lagayenge

    // Decimal formatting (Zero hatao agar nahi hai)
    val formattedPrice = if (absRupees % 1.0 == 0.0) {
        String.format("%.0f", absRupees)
    } else {
        String.format("%.2f", absRupees)
    }

    return when {
        this > 0 -> "+$formattedPrice" // Positive: Leny hen
        this < 0 -> "-$formattedPrice" // Negative: Deny hen
        else -> "Rs. 0"                     // Zero
    }
}



fun TextView.setBalanceWithColorRupee(amountPaisa: Long, prefix: String = "") {

    // 1. Text Set Karen (Sign wala function use kr k)
    val signedAmount = amountPaisa.toSignedBalanceRupee()
    this.text = "$prefix$signedAmount"

    // 2. Context len (Color uthane k liye)
    val context = this.context

    // 3. Color Logic
    val colorRes = when {
        amountPaisa > 0 -> R.color.green // Positive (+) = Leny hen (Green)
        amountPaisa < 0 -> R.color.red   // Negative (-) = Deny hen (Red)
        else -> R.color.black              // Zero
    }

    // 4. Color Apply Karen
    this.setTextColor(ContextCompat.getColor(context, colorRes))
}

fun TextView.setBalanceWithColor(amountPaisa: Long) {

    // Calculate Rupees (Rounded)
    val rupees = (amountPaisa / 100.0).roundToLong()

    // Set Text with Commas (e.g. Rs. 5,000)
    text = "Rs. %,d".format(rupees)

    // Set Color based on Positive/Negative
    if (amountPaisa >= 0) {
        setTextColor(ContextCompat.getColor(context, R.color.green_700)) // Ya jo apka color ho
    } else {
        setTextColor(ContextCompat.getColor(context, R.color.red))
    }
}