package com.miassolutions.milkledger.features.expense.ui.form

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemPersonalExpenseBinding
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent

class PersonalExpenseAdapter(
    private val onDraftChanged: (String, DraftField, String) -> Unit,
    private val onCommit: (String) -> Unit,
    private val onRemove: (String) -> Unit
) : ListAdapter<PersonalExpenseUi, PersonalExpenseAdapter.VH>(Diff) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).id.hashCode().toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPersonalExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val b: ItemPersonalExpenseBinding) : RecyclerView.ViewHolder(b.root) {

        // Listeners ko variable me rakha taake remove kar saken
        private val titleWatcher = object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                if (b.etPersonalTitle.hasFocus()) {
                    onDraftChanged(getItem(bindingAdapterPosition).id, DraftField.TITLE, s.toString())
                }
            }
        }

        private val amountWatcher = object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                if (b.etPersonalAmount.hasFocus()) {
                    onDraftChanged(getItem(bindingAdapterPosition).id, DraftField.AMOUNT, s.toString())
                }
            }
        }

        fun bind(item: PersonalExpenseUi) = with(b) {
            // 1. Remove old listeners to prevent loops
            etPersonalTitle.removeTextChangedListener(titleWatcher)
            etPersonalAmount.removeTextChangedListener(amountWatcher)
            etPersonalTitle.onFocusChangeListener = null
            etPersonalAmount.onFocusChangeListener = null

            // 2. Set Data (Only if not focused, to prevent cursor jumps)
            if (!etPersonalTitle.hasFocus()) etPersonalTitle.setTextIfDifferent(item.title)
            if (!etPersonalAmount.hasFocus()) etPersonalAmount.setTextIfDifferent(item.amount)

            // 3. Add Listeners
            etPersonalTitle.addTextChangedListener(titleWatcher)
            etPersonalAmount.addTextChangedListener(amountWatcher)

            // Commit on Focus Loss (UI Sync k liye)
            val focusListener = android.view.View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onCommit(getItem(bindingAdapterPosition).id)
                }
            }
            etPersonalTitle.onFocusChangeListener = focusListener
            etPersonalAmount.onFocusChangeListener = focusListener

            btnRemove.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    onRemove(getItem(bindingAdapterPosition).id)
                }
            }
        }
    }

    // Helper class to reduce boilerplate
    open class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {}
    }

    object Diff : DiffUtil.ItemCallback<PersonalExpenseUi>() {
        override fun areItemsTheSame(old: PersonalExpenseUi, new: PersonalExpenseUi) = old.id == new.id
        override fun areContentsTheSame(old: PersonalExpenseUi, new: PersonalExpenseUi) = old == new
    }
}

/**
 * 🔹 Ye enum is liye banaya:
 * taake ViewModel ko pata ho kis field ka draft update hua
 */

