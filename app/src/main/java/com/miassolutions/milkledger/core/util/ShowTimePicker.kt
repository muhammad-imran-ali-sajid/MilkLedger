package com.miassolutions.milkledger.core.util


import androidx.fragment.app.FragmentManager
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.LocalTime

fun showMaterialTimePicker(
    fragmentManager: FragmentManager,
    initialTime: LocalTime = LocalTime.now(),
    is24Hour: Boolean = true,
    onPicked: (LocalTime) -> Unit
) {
    val picker = MaterialTimePicker.Builder()
        .setTimeFormat(if (is24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
        .setHour(initialTime.hour)
        .setMinute(initialTime.minute)
        .setTitleText("Select time")
        .build()

    picker.addOnPositiveButtonClickListener {
        val selectedTime = LocalTime.of(picker.hour, picker.minute)
        onPicked(selectedTime)
    }

    picker.show(fragmentManager, "MaterialTimePicker")
}

