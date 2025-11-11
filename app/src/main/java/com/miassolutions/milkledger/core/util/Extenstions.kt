package com.miassolutions.milkledger.core.util

import android.graphics.Color
import android.view.View
import androidx.core.graphics.toColorInt
import java.time.LocalDate


fun Double.toRoundedStr(format: String = "%.2f"): String {
    return String.format(format, this)
}

fun Double.toPriceStr(format: String = "%.0f"): String {
    return String.format(format, this)
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}


fun LocalDate.isToday(): Boolean = this == LocalDate.now()






