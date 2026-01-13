package com.miassolutions.milkledger.utils.helper

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.miassolutions.milkledger.utils.extensions.toMilkAmount

fun handleZeroData(value: Double): String {
    return if (value == 0.0) {
        "--"
    } else {
        value.toMilkAmount("%.2f")
    }
}


fun textColor(amount: Double): Int {
    // Only consider the whole number part of the balance
    val wholeAmount = amount.toLong()

    return when {
        // If the whole number is less than 0 (e.g., -1, -2, etc.)
        wholeAmount < 0 -> "#bd0606".toColorInt() // Red for clearly negative

        // If the whole number is 0 (e.g., anything from -0.99 to +0.99)
        wholeAmount == 0L -> Color.BLACK // Black for zero or near-zero

        // Otherwise, it is a positive whole number (e.g., 1, 2, etc.)
        else -> "#008800".toColorInt() // Green for positive
    }
}


