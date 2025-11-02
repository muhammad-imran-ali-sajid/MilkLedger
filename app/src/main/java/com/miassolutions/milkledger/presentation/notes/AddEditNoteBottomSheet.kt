package com.miassolutions.milkledger.presentation.notes

import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.core.alarm.AlarmHelper
import com.miassolutions.milkledger.core.util.requestExactAlarmPermissionIfNeeded
import com.miassolutions.milkledger.core.util.showExpenseDatePicker
import com.miassolutions.milkledger.core.util.showMaterialTimePicker
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.databinding.BottomSheetAddEditNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddEditNoteBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<NotesViewModel>()
    private var currentNote: NoteEntity? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private var selectedDate: LocalDate? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomSheetAddEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        setupUI()
        setupObservers()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showSnackbar("Notification permission granted")
        }
    }


    private fun setupUI() {
        // Get note from arguments (for edit mode)
        currentNote = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("note", NoteEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("note")
        }



        currentNote?.let { note ->
            binding.etNoteTitle.setText(note.title)
            binding.etNoteContent.setText(note.content)
            note.alarmDate?.let {
                binding.tvAlarmDateAndTime.text = it.format(dateFormatter)
            }
            binding.btnSaveNote.text = "Update Note"
        }

        // Pick alarm date
        binding.tvAlarmDateAndTime.setOnClickListener {
            showExpenseDatePicker(
                isAuthorized = true,
                initialDate = LocalDate.now(),
                useConstraints = false,
                onPicked = { pickedDate ->
                    showMaterialTimePicker(
                        fragmentManager = parentFragmentManager,
                        initialTime = LocalTime.now()
                    ) { pickedTime ->
                        val alarmDateTime = pickedDate.atTime(pickedTime)
                        selectedDate = pickedDate
                        binding.tvAlarmDateAndTime.text =
                            "${pickedDate.toDisplayFormat()} ${pickedTime}"

                        // Ask permission and schedule alarm
                        checkAndScheduleAlarm(alarmDateTime)
                    }


                }
            )
        }

        // Save note
        binding.btnSaveNote.setOnClickListener {
            val title = binding.etNoteTitle.text.toString().trim()
            val content = binding.etNoteContent.text.toString().trim()

            if (title.isEmpty()) {
                showSnackbar("Please enter a title")
                return@setOnClickListener
            }

            val newNote = currentNote?.copy(
                title = title,
                content = content,
                alarmDate = selectedDate
            ) ?: NoteEntity(
                title = title,
                content = content,
                alarmDate = selectedDate
            )

            requireContext().requestExactAlarmPermissionIfNeeded()

            selectedDate?.let { date ->
                val alarmDateTime = date.atTime(10, 11) // 8 AM reminder (you can change)
                val alarmHelper = AlarmHelper(requireContext())
                alarmHelper.scheduleAlarm(
                    noteId = newNote.id,
                    title = newNote.title,
                    content = newNote.content,
                    triggerAt = alarmDateTime
                )

                viewModel.addOrUpdateNote(newNote)
            }
        }
    }

    private fun checkAndScheduleAlarm(alarmDateTime: LocalDateTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alarmManager = requireContext().getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                showSnackbar("Please allow exact alarms in settings.")
                return
            }

            if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                return
            }
        }

        // When note is actually saved (in btnSaveNote click), schedule will happen.
        selectedDate = alarmDateTime.toLocalDate()
    }


    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventFlow.collect { event ->
                    when (event) {
                        is NoteUiEvent.NoteSaved -> {
                            showSnackbar("Note saved")
                            dismiss()
                        }

                        is NoteUiEvent.ShowMessage -> {
                            showSnackbar(event.message)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(note: NoteEntity? = null): AddEditNoteBottomSheet {
            return AddEditNoteBottomSheet().apply {
                note?.let {
                    arguments = Bundle().apply {
                        putParcelable("note", it)
                    }
                }
            }
        }
    }
}
