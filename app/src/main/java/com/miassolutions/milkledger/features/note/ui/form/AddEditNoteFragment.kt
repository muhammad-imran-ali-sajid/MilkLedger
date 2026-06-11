package com.miassolutions.milkledger.features.note.ui.form


import android.Manifest
import android.R.attr.data
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.core.net.toUri

@AndroidEntryPoint
class AddEditNoteFragment : BaseFragment<FragmentAddEditNoteBinding>(
    FragmentAddEditNoteBinding::inflate
) {
    
    private fun openExactAlarmSettingsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(AlarmManager::class.java)
            
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${requireContext().packageName}".toUri()
                }
                startActivity(intent)
            }
        }
    }
    
    private fun openFullScreenIntentSettingsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager =
                requireContext().getSystemService(NotificationManager::class.java)
            
            if (!notificationManager.canUseFullScreenIntent()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:${requireContext().packageName}")
                }
                startActivity(intent)
            }
        }
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasNotificationPermission) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        
        openExactAlarmSettingsIfNeeded()
        openFullScreenIntentSettingsIfNeeded()
        
        viewModel.onEvent(AddEditNoteUiEvent.OnAlarmLayoutClick)
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
        
        // Get current time
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(clockFormat)
            .setHour(currentHour)      // ← current hour
            .setMinute(currentMinute)  // ← current minute
            .setTitleText("Select Time")
            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
            .build()
        
        timePicker.addOnPositiveButtonClickListener {
            // 1. Local Calendar instance
            val finalCalendar = Calendar.getInstance()
            
            // 2. Convert the dateMillis (UTC) to local date components
            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCalendar.timeInMillis = dateMillis
            
            finalCalendar.set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
            finalCalendar.set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
            finalCalendar.set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
            
            // 3. Set the user‑selected time
            finalCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            finalCalendar.set(Calendar.MINUTE, timePicker.minute)
            finalCalendar.set(Calendar.SECOND, 0)
            finalCalendar.set(Calendar.MILLISECOND, 0)
            
            // 4. Validate future time
            if (finalCalendar.timeInMillis <= System.currentTimeMillis()) {
                showSnackbar("Please select a future time")
            } else {
                viewModel.onEvent(AddEditNoteUiEvent.OnAlarmSet(finalCalendar.timeInMillis))
            }
        }
        
        timePicker.show(childFragmentManager, "TimePicker")
    }
}