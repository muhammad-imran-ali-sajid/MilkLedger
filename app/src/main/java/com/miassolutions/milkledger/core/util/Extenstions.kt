package com.miassolutions.milkledger.core.util

import android.view.View


fun Double.toRoundedStr(): String {
    return "%.1f".format(this)
}

fun View.hide(){
    this.visibility = View.GONE
}

fun View.show(){
    this.visibility = View.VISIBLE
}



