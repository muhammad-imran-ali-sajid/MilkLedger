package com.miassolutions.milkledger.features.note.ui.form


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat.is24HourFormat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentAddEditNoteBinding
import com.miassolutions.milkledger.utils.extensions.collectEffect
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.TimeZone

@AndroidEntryPoint
class AddEditNoteFragment : BaseFragment<FragmentAddEditNoteBinding>(
    FragmentAddEditNoteBinding::inflate
) {


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission mil gai, ab Alarm Dialog kholen
            viewModel.onEvent(AddEditNoteUiEvent.OnAlarmLayoutClick)
        } else {
            showSnackbar("Notifications are required for alarms to work")
        }
    }

    private val viewModel: AddEditNoteViewModel by viewModels()

    override fun setupViews() {
        super.setupViews()
        // Initial setup agar kuch ho
    }

    private fun checkPermissionAndOpenAlarm() {
        // Android 13 (Tiramisu) se upar Notification permission chahiye
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission hai -> Dialog kholo
                viewModel.onEvent(AddEditNoteUiEvent.OnAlarmLayoutClick)
            } else {
                // Permission nahi hai -> Request kro
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 ya neeche -> Direct Dialog kholo
            viewModel.onEvent(AddEditNoteUiEvent.OnAlarmLayoutClick)
        }
    }

    override fun setupListeners() = with(binding) {
        super.setupListeners()

        // 1. Text Listeners
        etNoteTitle.doAfterTextChanged {
            viewModel.onEvent(AddEditNoteUiEvent.OnTitleChange(it.toString()))
        }

        etNoteContent.doAfterTextChanged {
            viewModel.onEvent(AddEditNoteUiEvent.OnContentChange(it.toString()))
        }

        // 🔥 FIX 4: Sirf ye wala listener rakhein. Neeche wala delete kar dein!
        // Is se Permission check hogi -> Phir ViewModel call hoga
        layoutAlarm.setOnClickListener {
            checkPermissionAndOpenAlarm()
        }

        // ❌ ERROR WAS HERE:
        // Aapne yahan dobara layoutAlarm.setOnClickListener lagaya hua tha
        // jo permission logic ko bypass kar raha tha. Usay delete kr diya gya hai.

        // Long click to remove
        layoutAlarm.setOnLongClickListener {
            viewModel.onEvent(AddEditNoteUiEvent.OnRemoveAlarm)
            true
        }

        // Save
        btnSaveNote.setOnClickListener {
            viewModel.onEvent(AddEditNoteUiEvent.OnSaveClick)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // --- STATE OBSERVATION ---
        collectFlow(viewModel.uiState) { state ->
            binding.apply {

                // Cursor Jumping rokne k liye check zaroori hai
                if (etNoteTitle.text.toString() != state.title) {
                    etNoteTitle.setText(state.title)
                }

                if (etNoteContent.text.toString() != state.content) {
                    etNoteContent.setText(state.content)
                }

                tvAlarmDateAndTime.text = state.alarmDisplayString

                // Button Loading State
                btnSaveNote.isEnabled = !state.isLoading
                btnSaveNote.text = if (state.isLoading) "Saving..." else "Save note"
            }
        }

        // --- EFFECT OBSERVATION ---
        collectEffect(viewModel.uiEffect) { effect ->
            when (effect) {
                AddEditNoteUiEffect.NavigateBack -> {
                    findNavController().popBackStack()
                }

                is AddEditNoteUiEffect.ShowSnackbar -> {
                    showSnackbar(effect.message)
                }

                is AddEditNoteUiEffect.OpenDateTimePicker -> {
                    openDateTimePicker(effect.currentSelection)
                }
            }
        }
    }

    // --- DATE & TIME PICKER LOGIC ---
    private fun openDateTimePicker(currentMillis: Long?) {
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        // 1. Setup Date Picker Constraints (Past date disable)
        val constraints = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Reminder Date")
            .setSelection(currentMillis ?: today)
            .setCalendarConstraints(constraints)
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDateMillis ->
            // Date select hone k baad Time Picker kholen
            openTimePicker(selectedDateMillis)
        }

        datePicker.show(childFragmentManager, "DatePicker")
    }

    private fun openTimePicker(dateMillis: Long) {
        val isSystem24Hour = is24HourFormat(requireContext())
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(9)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        timePicker.addOnPositiveButtonClickListener {
            // 1. Local Calendar ka instance lein
            val finalCalendar = Calendar.getInstance()

            // 2. Date set karein (UTC se Local conversion)
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = dateMillis

            finalCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
            finalCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
            finalCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))

            // 3. Time set karein (Jo user ne select kia)
            finalCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            finalCalendar.set(Calendar.MINUTE, timePicker.minute)
            finalCalendar.set(Calendar.SECOND, 0)
            finalCalendar.set(Calendar.MILLISECOND, 0)

            // 4. Check karein k waqt guzar to nahi gaya?
            if (finalCalendar.timeInMillis <= System.currentTimeMillis()) {
                showSnackbar("Please select a future time")
            } else {
                // ViewModel ko bhejen
                viewModel.onEvent(AddEditNoteUiEvent.OnAlarmSet(finalCalendar.timeInMillis))
            }
        }

        timePicker.show(childFragmentManager, "TimePicker")
    }
}