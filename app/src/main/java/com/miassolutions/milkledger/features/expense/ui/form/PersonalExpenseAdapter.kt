package com.miassolutions.milkledger.features.expense.ui.form

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
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

    override fun getItemId(position: Int): Long {
        return getItem(position).id.hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemPersonalExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(
        private val b: ItemPersonalExpenseBinding
    ) : RecyclerView.ViewHolder(b.root) {

        private var titleWatcher: TextWatcher? = null
        private var amountWatcher: TextWatcher? = null

        fun bind(item: PersonalExpenseUi) = with(b) {

            // 🔹 IMPORTANT:
            // Purane listeners remove — warna multiple callbacks lag jate hain
            etPersonalTitle.removeTextChangedListener(titleWatcher)
            etPersonalAmount.removeTextChangedListener(amountWatcher)

            // 🔹 Only update UI when user is NOT typing
            if (!etPersonalTitle.hasFocus()) {
                etPersonalTitle.setTextIfDifferent(item.title)
            }

            if (!etPersonalAmount.hasFocus()) {
                etPersonalAmount.setTextIfDifferent(item.amount)
            }

            // 🔹 Draft typing — NO RecyclerView update
            titleWatcher = etPersonalTitle.doAfterTextChanged {
                if (etPersonalTitle.hasFocus()) {
                    onDraftChanged(item.id, DraftField.TITLE, it.toString())
                }
            }

            amountWatcher = etPersonalAmount.doAfterTextChanged {
                if (etPersonalAmount.hasFocus()) {
                    onDraftChanged(item.id, DraftField.AMOUNT, it.toString())
                }
            }

            // 🔹 Commit only when focus lost (professional pattern)
            etPersonalTitle.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) onCommit(item.id)
            }

            etPersonalAmount.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) onCommit(item.id)
            }

            btnRemove.setOnClickListener {
                onRemove(item.id)
            }
        }
    }

    object Diff : DiffUtil.ItemCallback<PersonalExpenseUi>() {
        override fun areItemsTheSame(old: PersonalExpenseUi, new: PersonalExpenseUi) =
            old.id == new.id

        override fun areContentsTheSame(old: PersonalExpenseUi, new: PersonalExpenseUi) =
            old.title == new.title && old.amount == new.amount
    }
}

/**
 * 🔹 Ye enum is liye banaya:
 * taake ViewModel ko pata ho kis field ka draft update hua
 */
enum class DraftField {
    TITLE,
    AMOUNT
}
