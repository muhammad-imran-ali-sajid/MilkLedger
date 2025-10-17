package com.miassolutions.milkledger.core.util

import android.view.View


fun Double.toRoundedStr(format: String = "%.1f"): String {
    return String.format(format, this)
}

fun View.hide(){
    this.visibility = View.GONE
}

fun View.show(){
    this.visibility = View.VISIBLE
}



