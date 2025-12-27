package com.miassolutions.milkledger.core.extensions

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
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













