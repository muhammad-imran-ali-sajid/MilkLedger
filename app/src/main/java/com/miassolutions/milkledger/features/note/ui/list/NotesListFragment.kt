package com.miassolutions.milkledger.features.note.ui.list


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.core.ui.BaseFragment
import com.miassolutions.milkledger.databinding.FragmentNotesListBinding
import com.miassolutions.milkledger.utils.extensions.collectFlow
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteListFragment : BaseFragment<FragmentNotesListBinding>(
    FragmentNotesListBinding::inflate
) {

    private val viewModel: NoteListViewModel by viewModels()

    private val adapter by lazy {
        NoteListAdapter { noteId ->
            // Navigate to Edit Mode
            val action = NoteListFragmentDirections.actionNoteListFragmentToAddEditNoteFragment(noteId)
            findNavController().navigate(action)
        }
    }

    override fun setupViews() {
        super.setupViews()

        // Setup RecyclerView with Staggered Layout (Google Keep Style)
        binding.rvNotes.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = this@NoteListFragment.adapter
        }

        // Setup Swipe to Delete
        setupSwipeToDelete()
    }

    override fun setupListeners() {
        super.setupListeners()

        binding.fabAddNote.setOnClickListener {
            // Navigate to Add Mode (ID is null)
            val action = NoteListFragmentDirections.actionNoteListFragmentToAddEditNoteFragment(null)
            findNavController().navigate(action)
        }
    }

    override fun setupObservers() {
        super.setupObservers()

        // Observe Notes
        collectFlow(viewModel.notes) { list ->
            adapter.submitList(list)

            // Empty State
            binding.layoutEmpty.isVisible = list.isEmpty()
            binding.rvNotes.isVisible = list.isNotEmpty()
        }

        // Observe Loading
        collectFlow(viewModel.isLoading) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }
    }

    // --- SWIPE TO DELETE LOGIC ---
    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val noteToDelete = adapter.currentList[position]

                // 1. Delete from ViewModel
                viewModel.deleteNote(noteToDelete)

                // 2. Show Undo Option
                Snackbar.make(binding.root, "Note deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        // Undo logic yahan implement ki ja sakti hai agar repository me restore function ho
                        // Filhal k liye hum sirf soft delete kar rahy hain
                    }
                    .show()
            }

            // Optional: Background color while swiping
            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = vh.itemView
                val background = ColorDrawable(Color.RED)

                // Draw Red background
                if (dX > 0) { // Swiping Right
                    background.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt(), itemView.bottom
                    )
                } else if (dX < 0) { // Swiping Left
                    background.setBounds(
                        itemView.right + dX.toInt(), itemView.top,
                        itemView.right, itemView.bottom
                    )
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)

                super.onChildDraw(c, rv, vh, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvNotes)
    }
}