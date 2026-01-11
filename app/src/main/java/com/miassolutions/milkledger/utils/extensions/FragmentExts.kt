package com.miassolutions.milkledger.utils.extensions

import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.utils.util.DatePickerLogic
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

fun Fragment.showDeleteActionDialog(
    title: String = "Caution!!",
    message: String = "Are you sure to delete this record?",
    onDeleteConfirmed: (() -> Unit)?
) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton("OK") { d, _ ->
            if (onDeleteConfirmed != null) {
                onDeleteConfirmed()
            }
            d.dismiss()
        }
        .setNegativeButton("Cancel", null)
        .show()
}


fun Fragment.showLedgerDatePicker(
    initialDate: LocalDate = LocalDate.now(),
    onPicked: (LocalDate) -> Unit
) {
    val fm = parentFragmentManager

    fm.findFragmentByTag("DATE_PICKER_TAG")?.let {
        fm.beginTransaction().remove(it).commitNow()
    }

    // ✅ Convert LocalDate → UTC midnight millis
    val initialMillis = initialDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

    val constraints = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now())
        .build()

    val picker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select Date")
        .setSelection(initialMillis)
        .setCalendarConstraints(constraints)
        .build()

    picker.addOnPositiveButtonClickListener { millis ->
        // ✅ Convert back from UTC → LocalDate
        val selectedDate = Instant.ofEpochMilli(millis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()

        onPicked(selectedDate)
    }

    picker.show(fm, "DATE_PICKER_TAG")
}


