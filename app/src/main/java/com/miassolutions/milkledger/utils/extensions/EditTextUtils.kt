package com.miassolutions.milkledger.utils.extensions

import android.widget.EditText
import androidx.appcompat.widget.AppCompatEditText

fun autoSelectOnFocus(editText: EditText) {
    editText.setSelectAllOnFocus(true)
    editText.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) (v as EditText).selectAll()
    }
}


// Helper extension
fun AppCompatEditText.setTextIfDifferent(text: String?) {
    val newText = text.orEmpty()
    if (this.text.toString() != newText) {
        setText(newText)
    }
}