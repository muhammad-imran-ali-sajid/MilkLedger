package com.miassolutions.milkledger.utils.extensions


import android.widget.TextView
import androidx.core.content.ContextCompat
import com.miassolutions.milkledger.R
import kotlin.math.abs

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
        this > 0 -> "+ Rs. $formattedPrice" // Positive: Leny hen
        this < 0 -> "- Rs. $formattedPrice" // Negative: Deny hen
        else -> "Rs. 0"                     // Zero
    }
}

fun TextView.setBalanceWithColor(amountPaisa: Long, prefix: String = "") {

    // 1. Text Set Karen (Sign wala function use kr k)
    val signedAmount = amountPaisa.toSignedBalance()
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