package com.miassolutions.milkledger.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedString(prefix: String = "db_"): String {
    val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val formattedDate = formatter.format(Date(this))
    return "$prefix$formattedDate"
}
