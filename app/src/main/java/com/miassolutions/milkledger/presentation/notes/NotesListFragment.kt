package com.miassolutions.milkledger.presentation.notes

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.databinding.FragmentNotesListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesListFragment :
    BaseFragment<FragmentNotesListBinding>(FragmentNotesListBinding::inflate) {

    private val viewModel by viewModels<NotesViewModel>()
    private lateinit var adapter: NotesListAdapter

    override fun setupViews() {
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = NotesListAdapter(
            onItemClick = { note ->
                // Navigate to Add/Edit bottom sheet or fragment
                // e.g. findNavController().navigate(...)
                viewModel.addOrUpdateNote(note)
            },
            onDeleteClick = { note ->
                showDeleteConfirmation(note)
            },
            onCheckChanged = { note, isChecked ->
                viewModel.toggleIsDone(note.id, isChecked)
            }
        )

        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotesListFragment.adapter
        }
    }

    override fun setupListeners() {
        binding.fabAddNote.setOnClickListener {
//            viewModel.addOrUpdateNote()
            // Navigate to add note fragment or bottom sheet
            AddEditNoteBottomSheet.newInstance(null).show(parentFragmentManager, "AddEditNote")

        }
    }

    override fun setupObservers() {
        // Collect UI state

        viewModel.uiState.collectState { state ->
            binding.progressBar.visibility =
                if (state.isLoading) View.VISIBLE else View.GONE

            adapter.submitList(state.notes)

            state.error?.let { showSnackbar(it) }
        }


        // Collect UI events

        viewModel.eventFlow.collectState { event ->
            when (event) {
                is NoteUiEvent.NoteDeleted -> {
                    showUndoSnackbar(event.noteEntity)
                }

                NoteUiEvent.NoteSaved -> {
                    showSnackbar("Note saved")
                }

                is NoteUiEvent.ShowMessage -> {
                    showSnackbar(event.message)
                }

                else -> Unit
            }

        }
    }

    private fun showDeleteConfirmation(note: NoteEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note?")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNote(note)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUndoSnackbar(note: NoteEntity) {
        showSnackbar("Note Deleted", Snackbar.LENGTH_LONG) {
            viewModel.addOrUpdateNote(note)
        }
    }
}
