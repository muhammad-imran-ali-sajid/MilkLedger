package com.miassolutions.milkledger.utils.extensions

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import java.time.LocalDate



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
    return String.format(format, this)
}

fun Double.toLongPaisa() = (this * 100).toLong()

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}


