package com.miassolutions.milkledger.core.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.miassolutions.milkledger.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedString(prefix: String = "db_"): String {
    val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val formattedDate = formatter.format(Date(this))
    return "$prefix$formattedDate"
}




fun EditText.clearTextWatchers() {
    val watchers = this.tag as? MutableList<TextWatcher> ?: mutableListOf()
    watchers.forEach { removeTextChangedListener(it) }
    watchers.clear()
    this.tag = watchers
}

fun EditText.addTextWatcher(watcher: TextWatcher) {
    val watchers = (this.tag as? MutableList<TextWatcher>) ?: mutableListOf()
    watchers.add(watcher)
    this.tag = watchers
    addTextChangedListener(watcher)
}



