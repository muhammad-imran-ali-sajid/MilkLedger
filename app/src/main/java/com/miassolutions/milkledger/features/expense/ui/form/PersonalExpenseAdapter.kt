package com.miassolutions.milkledger.features.expense.ui.form

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemPersonalExpenseBinding

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
        private val binding: ItemPersonalExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val titleWatcher = object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                if (!binding.etPersonalTitle.hasFocus()) return

                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return

                onDraftChanged(
                    getItem(position).id,
                    DraftField.TITLE,
                    s.toString()
                )
            }
        }

        private val amountWatcher = object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                if (!binding.etPersonalAmount.hasFocus()) return

                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return

                onDraftChanged(
                    getItem(position).id,
                    DraftField.AMOUNT,
                    s.toString()
                )
            }
        }

        fun bind(item: PersonalExpenseUi) = with(binding) {

            // Remove old listeners before binding recycled data
            etPersonalTitle.removeTextChangedListener(titleWatcher)
            etPersonalAmount.removeTextChangedListener(amountWatcher)

            etPersonalTitle.onFocusChangeListener = null
            etPersonalAmount.onFocusChangeListener = null


            // Update text only when needed
            if (!etPersonalTitle.hasFocus()) {
                etPersonalTitle.setTextIfChanged(item.title)
            }

            if (!etPersonalAmount.hasFocus()) {
                etPersonalAmount.setTextIfChanged(item.amount)
            }


            // Restore listeners
            etPersonalTitle.addTextChangedListener(titleWatcher)
            etPersonalAmount.addTextChangedListener(amountWatcher)


            // Sync latest draft into UiState when focus leaves
            val focusListener = android.view.View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) return@OnFocusChangeListener

                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@OnFocusChangeListener

                onCommit(getItem(position).id)
            }

            etPersonalTitle.onFocusChangeListener = focusListener
            etPersonalAmount.onFocusChangeListener = focusListener


            // Remove row
            btnRemove.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }

                onRemove(getItem(position).id)
            }
        }
    }

    private fun EditText.setTextIfChanged(value: String) {
        if (text.toString() == value) return

        setText(value)
        setSelection(text.length)
    }

    open class SimpleTextWatcher : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) = Unit

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) = Unit

        override fun afterTextChanged(s: Editable?) = Unit
    }

    object Diff : DiffUtil.ItemCallback<PersonalExpenseUi>() {

        override fun areItemsTheSame(
            oldItem: PersonalExpenseUi,
            newItem: PersonalExpenseUi
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: PersonalExpenseUi,
            newItem: PersonalExpenseUi
        ): Boolean {
            return oldItem == newItem
        }
    }
}