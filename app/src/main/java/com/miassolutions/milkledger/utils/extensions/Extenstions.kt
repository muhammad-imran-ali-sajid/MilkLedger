package com.miassolutions.milkledger.utils.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import java.time.LocalDate
import kotlin.math.roundToLong


// --- 1. VISIBILITY ---


fun View.invisible() {
    this.visibility = View.INVISIBLE
}

// Smart Visibility: true = Show, false = Hide
fun View.visible(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

// --- 2. KEYBOARD HANDLING ---

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Fragment.hideKeyboard() {
    view?.hideKeyboard()
}

fun Double.toMilkAmount(format: String = "%.2f"): String {
    return String.format(format, this)
}

fun Double.toPrice(format: String = "%.0f"): String {
    val rupees = (this / 100.0).roundToLong()

    // "%,d" automatic commas laga deta hai (e.g., 1,250)
    return "%,d".format(rupees)
}


fun Double.toLongPaisa() = (this * 100).toLong()




// --- 1. UI (String) to Database (Paisa/Long) ---

// User ne "5000" likha -> Database me 500000 jayega
fun String?.toPaisa(): Long {
    if (this.isNullOrBlank()) return 0L
    val doubleValue = this.toDoubleOrNull() ?: 0.0
    return (doubleValue * 100).toLong()
}

// Calculation result (Double) -> Database (Paisa/Long)
fun Double.toPaisa(): Long {
    return (this * 100).toLong()
}


// --- 2. Database (Paisa/Long) to UI (Double/String) ---

// Database se 500000 aaya -> UI me 5000.0 banega
fun Long.toRupees(): Double {
    return this / 100.0
}

// Database se 500000 aaya -> UI string "5000" banega (Point k baad zero hata kar)
fun Long.toRupeesStr(): String {
    val rupees = this / 100.0
    // Agar .0 hai to hata do, warna dikhao (e.g. 50.5)
    return if (rupees % 1.0 == 0.0) {
        String.format("%.0f", rupees)
    } else {
        String.format("%.2f", rupees)
    }
}




fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}


