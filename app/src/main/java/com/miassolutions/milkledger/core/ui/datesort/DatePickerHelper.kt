package com.miassolutions.milkledger.core.ui.datesort



import android.app.DatePickerDialog
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

object DatePickerHelper {

    fun pickSingleDate(fragment: Fragment, onPicked: (LocalDate) -> Unit) {
        val now = LocalDate.now()
        val picker = DatePickerDialog(
            fragment.requireContext(),
            { _, year, month, dayOfMonth ->
                onPicked(LocalDate.of(year, month + 1, dayOfMonth))
            },
            now.year, now.monthValue - 1, now.dayOfMonth
        )
        picker.show()
    }

    fun pickWeek(fragment: Fragment, onPicked: (start: LocalDate, end: LocalDate) -> Unit) {
        pickSingleDate(fragment) { picked ->
            val start = picked.with(DayOfWeek.MONDAY)
            val end = picked.with(DayOfWeek.SUNDAY)
            onPicked(start, end)
        }
    }

    fun pickMonth(fragment: Fragment, onPicked: (start: LocalDate, end: LocalDate, label: String) -> Unit) {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select any date in month")
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply { timeInMillis = selection }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val start = LocalDate.of(year, month, 1)
            val end = start.withDayOfMonth(start.lengthOfMonth())
            val label = start.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
            onPicked(start, end, label)
        }

        picker.show(fragment.parentFragmentManager, "MonthPicker")
    }

    fun formatRange(start: LocalDate, end: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM")
        return if (start == end) start.format(formatter)
        else "${start.format(formatter)} → ${end.format(formatter)}"
    }
}
