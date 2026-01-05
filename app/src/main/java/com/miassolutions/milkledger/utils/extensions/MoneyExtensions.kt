package com.miassolutions.milkledger.utils.extensions


import java.text.NumberFormat
import java.util.Locale

// --- 1. MONEY (Long -> String) ---

// Database ka Long (Paisa) -> UI ka String (Rs. 500.50)
fun Long.toPrice(showCurrency: Boolean = false): String {
    val rupees = this / 100.0
    // "%.2f" ka matlab point k baad 2 hindsy (500.50)
    // "%.0f" ka matlab point k baad kuch nahi (500)
    val formatted = String.format("%.0f", rupees)
    return if (showCurrency) "Rs. $formatted" else formatted
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