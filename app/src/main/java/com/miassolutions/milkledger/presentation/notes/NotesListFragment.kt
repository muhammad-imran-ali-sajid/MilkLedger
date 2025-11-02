package com.miassolutions.milkledger.presentation.notes

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
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
                AddEditNoteBottomSheet.newInstance(note)
                    .show(parentFragmentManager, "AddEditNote")
            },
            onDeleteClick = { note ->
                showDeleteConfirmation(note)
            },
            onCheckChanged = { note, isChecked ->
                viewModel.toggleIsDone(note.id, isChecked)
            }
        )

        binding.rvNotes.apply {
            adapter = this@NotesListFragment.adapter


            //Add scroll listener for FAB hide/show
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0 && binding.fabAddNote.isShown) {
                        binding.fabAddNote.animate().translationY(binding.fabAddNote.height.toFloat() + 50).alpha(0f).start()
                    } else if (dy < 0 && binding.fabAddNote.alpha == 0f) {
                        binding.fabAddNote.animate().translationY(0f).alpha(1f).start()
                    }
                }
            })
        }
    }


    override fun setupListeners() {
        binding.fabAddNote.setOnClickListener {
            AddEditNoteBottomSheet.newInstance(null).show(parentFragmentManager, "AddEditNote")
        }
        binding.etSearch.addTextChangedListener { editable ->
            viewModel.onSearchQueryChanged(editable?.toString().orEmpty())
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
