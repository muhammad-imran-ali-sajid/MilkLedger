package com.miassolutions.milkledger.presentation.notes

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
import com.miassolutions.milkledger.utils.alarm.AlarmScheduler
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.databinding.BottomSheetAddEditNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@AndroidEntryPoint
class AddEditNoteBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<NotesViewModel>()
    private var currentNote: NoteEntity? = null
    private var selectedDateTime: LocalDateTime? = null

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
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
//        // Get note from arguments (for edit mode)
//        currentNote = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            arguments?.getParcelable("note", NoteEntity::class.java)
//        } else {
//            @Suppress("DEPRECATION")
//            arguments?.getParcelable("note")
//        }
//
//        currentNote?.let { note ->
//            binding.etNoteTitle.setText(note.title)
//            binding.etNoteContent.setText(note.content)
//            note.alarmDateTime?.let {
//                binding.tvAlarmDateAndTime.text = it.format(dateTimeFormatter)
//                selectedDateTime = it // restore previous alarm time
//            }
//            binding.btnSaveNote.text = "Update Note"
//        }
//
//        // Pick alarm date
//        binding.tvAlarmDateAndTime.setOnClickListener {
//            val safeDate = selectedDateTime?.toLocalDate() ?: LocalDate.now()
//            val safeTime = selectedDateTime?.toLocalTime() ?: LocalTime.now()
//
//            showFutureDatePicker(
//                initialDate = safeDate,
//                onPicked = { pickedDate ->
//                    showMaterialTimePicker(
//                        fragmentManager = parentFragmentManager,
//                        initialTime = safeTime
//                    ) { pickedTime ->
//                        val alarmDateTime = pickedDate.atTime(pickedTime)
//                        if (alarmDateTime.isBefore(LocalDateTime.now())) {
//                            showSnackbar("Please select a future time.")
//                            return@showMaterialTimePicker
//                        }
//
//                        selectedDateTime = alarmDateTime
//                        binding.tvAlarmDateAndTime.text = alarmDateTime.format(dateTimeFormatter)
//                        checkAndScheduleAlarm(alarmDateTime, binding.etNoteTitle.text.toString(), binding.etNoteContent.text.toString())
//                    }
//                }
//            )
//        }
//
//
//        // Save note
//        binding.btnSaveNote.setOnClickListener {
//            val title = binding.etNoteTitle.text.toString().trim()
//            val content = binding.etNoteContent.text.toString().trim()
//
//
//            if (title.isEmpty()) {
//                showSnackbar("Please enter a title")
//                return@setOnClickListener
//            }
//
//            if (selectedDateTime == null) {
//                showSnackbar("Please set alarm date and time")
//                return@setOnClickListener
//            }
//
//            val newNote = currentNote?.copy(
//                title = title,
//                content = content,
//                alarmDateTime = selectedDateTime
//            ) ?: NoteEntity(
//                title = title,
//                content = content,
//                alarmDateTime = selectedDateTime
//            )
//
//            viewModel.addOrUpdateNote(newNote)
//        }
    }

    private fun checkAndScheduleAlarm(
        alarmDateTime: LocalDateTime,
        title: String,
        message: String
    ) {
        AlarmScheduler.scheduleExactAlarm(
            requireContext(),
            alarmDateTime,
            title,
            message
        )
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
                        is NoteUiEvent.ShowMessage -> showSnackbar(event.message)
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
//                        putParcelable("note", it)
                    }
                }
            }
        }
    }
}
