package com.miassolutions.milkledger.core.ui.extensions

import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Fragment.pickSingleDate(
    title: String = "Select Date",
    initialDate: LocalDate = LocalDate.now(),
    themeResId: Int? = null,
    tag: String = "SingleDatePicker",
    onPicked: (LocalDate) -> Unit
) {
    val picker = buildPicker(title, initialDate, themeResId)
    picker.addOnPositiveButtonClickListener {
        onPicked(it.toLocalDate())
    }
    picker.show(parentFragmentManager, tag)
}

fun Fragment.pickWeek(
    title: String = "Select any date in the week",
    initialDate: LocalDate = LocalDate.now(),
    themeResId: Int? = null,
    tag: String = "WeekPicker",
    onPicked: (start: LocalDate, end: LocalDate) -> Unit
) {
    val picker = buildPicker(title, initialDate, themeResId)
    picker.addOnPositiveButtonClickListener {
        val picked = it.toLocalDate()
        onPicked(picked.with(DayOfWeek.MONDAY), picked.with(DayOfWeek.SUNDAY))
    }
    picker.show(parentFragmentManager, tag)
}

fun Fragment.pickMonth(
    title: String = "Select any date in the month",
    initialDate: LocalDate = LocalDate.now(),
    themeResId: Int? = null,
    tag: String = "MonthPicker",
    onPicked: (start: LocalDate, end: LocalDate, label: String) -> Unit
) {
    val picker = buildPicker(title, initialDate, themeResId)
    picker.addOnPositiveButtonClickListener {
        val picked = it.toLocalDate()
        val start = picked.withDayOfMonth(1)
        val end = picked.withDayOfMonth(picked.lengthOfMonth())
        val label = start.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        onPicked(start, end, label)
    }
    picker.show(parentFragmentManager, tag)
}

fun formatDateRange(start: LocalDate, end: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMM")
    return if (start == end) start.format(formatter)
    else "${start.format(formatter)} → ${end.format(formatter)}"
}

// --------- Private helpers -----------

private fun buildPicker(title: String, initialDate: LocalDate, themeResId: Int?): MaterialDatePicker<Long> {
    val millis = initialDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()

    val builder = MaterialDatePicker.Builder.datePicker()
        .setTitleText(title)
        .setSelection(millis)

    themeResId?.let { builder.setTheme(it) }

    return builder.build()
}


private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.of("UTC")) // ⬅️ Fix here too
        .toLocalDate()

