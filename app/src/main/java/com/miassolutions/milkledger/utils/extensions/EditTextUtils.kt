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
fun AppCompatEditText.setTextIfDifferent(value: String) {
    if (text.toString() != value) {
        setText(value)
        setSelection(value.length)
    }
}