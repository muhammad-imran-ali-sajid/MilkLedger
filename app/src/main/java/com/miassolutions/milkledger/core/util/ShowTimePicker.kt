package com.miassolutions.milkledger.core.util


import android.app.TimePickerDialog
import android.content.Context
import java.time.LocalTime

fun showTimePicker(
    context: Context,
    initialTime: LocalTime = LocalTime.now(),
    onPicked: (LocalTime) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onPicked(LocalTime.of(hourOfDay, minute))
        },
        initialTime.hour,
        initialTime.minute,
        true // 24-hour format
    ).show()
}
