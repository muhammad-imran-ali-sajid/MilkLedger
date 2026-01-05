package com.miassolutions.milkledger.features.expense.ui.form

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemPersonalExpenseBinding
import com.miassolutions.milkledger.utils.extensions.setTextIfDifferent

class PersonalExpenseAdapter(
    private val onTitleChanged: (String, String) -> Unit,
    private val onAmountChanged: (String, String) -> Unit,
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

        fun bind(item: PersonalExpenseUi) = with(b) {

            // Only set text if different (prevents cursor jump)
            etPersonalTitle.setTextIfDifferent(item.title)
            etPersonalAmount.setTextIfDifferent(item.amount)

            // Update ViewModel ONLY when focus is lost
            etPersonalTitle.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    onTitleChanged(
                        item.id,
                        etPersonalTitle.text.toString()
                    )
                }
            }

            etPersonalAmount.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    onAmountChanged(
                        item.id,
                        etPersonalAmount.text.toString()
                    )
                }
            }

            btnRemove.setOnClickListener {
                onRemove(item.id)
            }
        }
    }

    object Diff : DiffUtil.ItemCallback<PersonalExpenseUi>() {
        override fun areItemsTheSame(
            oldItem: PersonalExpenseUi,
            newItem: PersonalExpenseUi
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: PersonalExpenseUi,
            newItem: PersonalExpenseUi
        ) = oldItem.title == newItem.title &&
                oldItem.amount == newItem.amount
    }
}
