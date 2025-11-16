package com.miassolutions.milkledger.core.util

import android.widget.EditText

fun autoSelectOnFocus(editText: EditText) {
    editText.setSelectAllOnFocus(true)
    editText.setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) (v as EditText).selectAll()
    }
}