package com.miassolutions.milkledger.utils.extensions

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun appDateFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM-yy", Locale.getDefault())
private fun appOnlyDateFormatter(): DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd-MM", Locale.getDefault())

fun LocalDate.toCompleteDateFormat(): String =
    this.format(appDateFormatter())

fun LocalDate.toDisplayDate(): String =
    this.format(appOnlyDateFormatter())


fun formatPeriodLabel(start: LocalDate, end: LocalDate): String {
    return if (start == end) start.toCompleteDateFormat()
    else "${start.toCompleteDateFormat()} to ${end.toCompleteDateFormat()}"
}