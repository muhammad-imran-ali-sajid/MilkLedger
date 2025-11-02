package com.miassolutions.milkledger.presentation.notes

import android.app.DatePickerDialog
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
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.databinding.BottomSheetAddEditNoteBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddEditNoteBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<NotesViewModel>()
    private var currentNote: NoteEntity? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

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
        // Get note from arguments (for edit mode)
        currentNote = arguments?.getParcelable("note")

        currentNote?.let { note ->
            binding.etNoteTitle.setText(note.title)
            binding.etNoteContent.setText(note.content)
            note.alarmDate?.let {
                binding.tvNoteDate.text = it.format(dateFormatter)
            }
            binding.btnSaveNote.text = "Update Note"
        }

        // Pick alarm date
        binding.tvAlarmDateAndTime.setOnClickListener {
            val today = LocalDate.now()
            val dialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val selected = LocalDate.of(year, month + 1, day)
                    binding.tvAlarmDateAndTime.text = selected.format(dateFormatter)
                },
                today.year, today.monthValue - 1, today.dayOfMonth
            )
            dialog.show()
        }

        // Save note
        binding.btnSaveNote.setOnClickListener {
            val title = binding.etNoteTitle.text.toString().trim()
            val content = binding.etNoteContent.text.toString().trim()
            val alarmDateText = binding.tvAlarmDateAndTime.text.toString().trim()
            val alarmDate =
                if (alarmDateText.isNotEmpty() && alarmDateText != "Select Date")
                    LocalDate.parse(alarmDateText, dateFormatter)
                else null

            if (title.isEmpty()) {
                showSnackbar("Please enter a title")
                return@setOnClickListener
            }

            val newNote = currentNote?.copy(
                title = title,
                content = content,
                alarmDate = alarmDate
            ) ?: NoteEntity(
                title = title,
                content = content,
                alarmDate = alarmDate
            )

            viewModel.addOrUpdateNote(newNote)
        }
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
