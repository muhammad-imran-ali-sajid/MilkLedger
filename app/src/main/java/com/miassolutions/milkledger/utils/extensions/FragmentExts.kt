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
    initialMillis: Long? = null,
    onDateSelected: (LocalDate) -> Unit
) {
    val constraintsBuilder = CalendarConstraints.Builder()
        .setValidator(DateValidatorPointBackward.now()) // Optional: Agar future date mana karni ho

    val datePicker = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select Date")
        .setSelection(initialMillis ?: MaterialDatePicker.todayInUtcMilliseconds())
        .setCalendarConstraints(constraintsBuilder.build())
        .build()

    datePicker.addOnPositiveButtonClickListener { selectionMillis ->
        // Convert UTC millis to Local Date
        val date = Instant.ofEpochMilli(selectionMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        onDateSelected(date)
    }

    datePicker.show(childFragmentManager, "DATE_PICKER")
}