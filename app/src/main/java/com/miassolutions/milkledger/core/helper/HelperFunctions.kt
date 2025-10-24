package com.miassolutions.milkledger.core.helper

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.miassolutions.milkledger.core.util.toRoundedStr

fun handleZeroData(value: Double): String {
    return if (value == 0.0) {
        "--"
    } else {
        value.toRoundedStr()
    }
}


fun textColor(amount: Double): Int {
    return when {
        amount < 0 -> Color.RED
        amount == 0.0 -> "#000000".toColorInt()
        else -> "#4CAF50".toColorInt() // Material green 500
    }
}


fun numberFormat(amount: Double): String {
    return when {
        amount > 0 -> "+${amount.toRoundedStr()}"
        else -> amount.toRoundedStr()
    }
}