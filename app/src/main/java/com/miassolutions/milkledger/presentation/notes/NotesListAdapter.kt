package com.miassolutions.milkledger.presentation.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.databinding.ItemNoteBinding


class NotesListAdapter(
    private val onItemClick: (NoteEntity) -> Unit,
    private val onDeleteClick: (NoteEntity) -> Unit,
    private val onCheckChanged: (NoteEntity, Boolean) -> Unit
) : ListAdapter<NoteEntity, NotesListAdapter.NoteViewHolder>(DiffCallback) {

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteEntity) = with(binding) {
            tvNoteTitle.text = note.title
            tvNoteContent.text = note.content
            tvNoteDate.text = note.createdDate.toString()
            tvAlarm.text = note.alarmDate?.toString() ?: "No Alarm"
            cbDone.isChecked = note.isDone

            root.setOnClickListener { onItemClick(note) }

            root.setOnLongClickListener { onDeleteClick(note); true }

            cbDone.setOnCheckedChangeListener(null)
            cbDone.isChecked = note.isDone
            cbDone.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(note, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }
}
