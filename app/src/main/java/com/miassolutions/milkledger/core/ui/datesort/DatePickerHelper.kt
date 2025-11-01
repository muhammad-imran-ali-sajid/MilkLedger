package com.miassolutions.milkledger.core.ui.extensions

import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun formatDateRange(start: LocalDate, end: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM")
    return if (start == end) start.format(formatter)
    else "${start.format(formatter)} → ${end.format(formatter)}"
}

fun LocalDate.formattedDate(pattern: String = "dd-MM-yy"): String {
    return this.format(DateTimeFormatter.ofPattern(pattern))
}


