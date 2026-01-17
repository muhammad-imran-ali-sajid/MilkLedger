package com.miassolutions.milkledger.utils.extensions

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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

fun Long.toCompleteDateFormatWithTime(): String {
    if (this == 0L) return ""
    val dateTime = Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()

    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy • hh:mm a", Locale.getDefault())
    return dateTime.format(formatter)
}


fun formatPeriodLabel(start: LocalDate, end: LocalDate): String {
    return if (start == end) start.toCompleteDateFormat()
    else "${start.toCompleteDateFormat()} to ${end.toCompleteDateFormat()}"
}