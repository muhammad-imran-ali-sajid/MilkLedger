package com.miassolutions.milkledger.core.helper

import android.graphics.Color
import androidx.core.graphics.toColorInt
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr

fun handleZeroData(value: Double): String {
    return if (value == 0.0) {
        "--"
    } else {
        value.toRoundedStr("%.2f")
    }
}


fun textColor(amount: Double): Int {
    return when {
        amount < 0 -> "#bd0606".toColorInt()
        amount == 0.00 -> Color.BLACK
        else -> "#2faD32".toColorInt() // Material green 500
    }
}


fun numberFormat(amount: Double): String {
    return when {
        amount > 0 -> "+${amount.toPriceStr()}"
        else -> amount.toPriceStr()
    }
}