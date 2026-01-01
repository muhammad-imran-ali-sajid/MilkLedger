package com.miassolutions.milkledger.utils.extensions

import androidx.fragment.app.Fragment
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
    isAuthorized: Boolean = true,
    initialDate: LocalDate = LocalDate.now(),
    useConstraints: Boolean = true, // toggle constraints ON/OFF
    onPicked: (LocalDate) -> Unit
) {
    // Build constraints only if requested
    val constraints = if (useConstraints) {
        DatePickerLogic().buildConstraints(isAuthorized)
    } else null

    // Convert LocalDate to UTC timestamp
    val initialTimestamp = initialDate
        .atStartOfDay(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()

    // Build the Material Date Picker
    val builder = MaterialDatePicker.Builder.datePicker()
        .setTitleText("Select Date")
        .setSelection(initialTimestamp)

    // Apply constraints only if they exist
    constraints?.let { builder.setCalendarConstraints(it) }

    val datePicker = builder.build()

    // Handle selected date
    datePicker.addOnPositiveButtonClickListener { selectedTimestamp ->
        val selectedDate = Instant.ofEpochMilli(selectedTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        onPicked(selectedDate)
    }

    // Show the picker
    datePicker.show(parentFragmentManager, "DATE_PICKER_TAG")
}