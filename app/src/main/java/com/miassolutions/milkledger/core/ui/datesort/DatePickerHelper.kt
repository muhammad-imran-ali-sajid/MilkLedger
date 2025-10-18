package com.miassolutions.milkledger.core.ui.datesort

import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object DatePickerHelper {

    /**
     * Picks a single date using MaterialDatePicker.
     */
    fun pickSingleDate(fragment: Fragment, onPicked: (LocalDate) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val picked = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            onPicked(picked)
        }

        picker.show(fragment.parentFragmentManager, "SingleDatePicker")
    }

    /**
     * Picks a date to determine the week. The user selects any date,
     * and we automatically determine the Monday→Sunday range.
     */
    fun pickWeek(fragment: Fragment, onPicked: (start: LocalDate, end: LocalDate) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select any date in the week")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val picked = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val start = picked.with(DayOfWeek.MONDAY)
            val end = picked.with(DayOfWeek.SUNDAY)

            onPicked(start, end)
        }

        picker.show(fragment.parentFragmentManager, "WeekPicker")
    }

    /**
     * Picks a month by letting user pick any date inside it.
     */
    fun pickMonth(fragment: Fragment, onPicked: (start: LocalDate, end: LocalDate, label: String) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select any date in the month")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val picked = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val start = picked.withDayOfMonth(1)
            val end = picked.withDayOfMonth(picked.lengthOfMonth())

            val label = start.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            onPicked(start, end, label)
        }

        picker.show(fragment.parentFragmentManager, "MonthPicker")
    }

    /**
     * Utility to format a range nicely.
     */
    fun formatRange(start: LocalDate, end: LocalDate): String {
        val sameDay = start == end
        val formatter = DateTimeFormatter.ofPattern("dd MMM")
        return if (sameDay) start.format(formatter)
        else "${start.format(formatter)} → ${end.format(formatter)}"
    }
}
