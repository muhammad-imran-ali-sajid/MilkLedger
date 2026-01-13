package com.miassolutions.milkledger.utils.extensions


import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

// --- 1. MONEY (Long -> String) ---

// Database ka Long (Paisa) -> UI ka String (Rs. 500.50)
fun Long.toPrice(showCurrency: Boolean = false): String {
    val rupees = (this / 100.0).roundToLong()

    // "%,d" automatic commas laga deta hai (e.g., 1,250)
    return "%,d".format(rupees)
}



fun String.toLongPaisa(): Long {
    return (this.toDouble() * 100).toLong()
}



// --- 2. MILK QUANTITY (Double -> String) ---

fun Double.toMilkAmount(): String {
    return String.format("%.2f", this) // e.g., "40.50"
}

// --- 3. GENERAL ---

fun Double.toRoundedString(): String {
    return String.format("%.2f", this)
}