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

    // 🔥 HARD FIX: remove previous picker if exists
    fm.findFragmentByTag("DATE_PICKER_TAG")?.let {
        fm.beginTransaction().remove(it).commitNow()
    }

    val zoneId = ZoneId.systemDefault()

    val initialMillis = initialDate
        .atStartOfDay(zoneId)
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
        val selectedDate = Instant.ofEpochMilli(millis)
            .atZone(zoneId)
            .toLocalDate()

        onPicked(selectedDate)
    }

    picker.show(fm, "DATE_PICKER_TAG")
}

