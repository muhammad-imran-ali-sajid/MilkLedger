package com.miassolutions.milkledger.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun appDateFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yy", Locale.getDefault())

fun LocalDate.toDisplayFormat(): String =
    this.format(appDateFormatter())