package com.miassolutions.milkledger.presentation.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.ui.FilterableDelegate
import com.miassolutions.milkledger.core.ui.FilterableList
import com.miassolutions.milkledger.core.util.dateFormatter
import com.miassolutions.milkledger.core.util.dateTimeFormatter
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.databinding.ItemNoteBinding
import java.time.LocalDate
import java.time.LocalDateTime


class NotesListAdapter(
    private val onItemClick: (NoteEntity) -> Unit,
    private val onDeleteClick: (NoteEntity) -> Unit,

) : ListAdapter<NoteEntity, NotesListAdapter.NoteViewHolder>(DiffCallback),
    FilterableList<NoteEntity> {

    private val filterDelegate = FilterableDelegate(this) { item, query ->
        item.title.lowercase().contains(query) || item.content.lowercase().contains(query)
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

//        fun bind(note: NoteEntity) = with(binding) {
//            tvNoteTitle.text = note.title
//            tvNoteContent.text = note.content
//            tvNoteDate.text = "Dated: ${note.createdDate.format(dateFormatter)}"
//            tvAlarm.text = note.alarmDateTime?.format(dateTimeFormatter) ?: "No Alarm"
//
//
//            val pastColor = "#FF9100".toColorInt()
//
//            if (LocalDateTime.now() > note.alarmDateTime) tillTitle.setBackgroundColor(pastColor) else {
//                tillTitle.setBackgroundColor("#00000000".toColorInt())
//            }
//
//            root.setOnClickListener { onItemClick(note) }
//
//            root.setOnLongClickListener { onDeleteClick(note); true }
//
//
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
//        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem.noteId == newItem.noteId

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }

    override fun setOriginalList(list: List<NoteEntity>) {
        filterDelegate.setOriginalList(list)
    }

    override fun filter(query: String) {
        filterDelegate.filter(query)
    }
}
