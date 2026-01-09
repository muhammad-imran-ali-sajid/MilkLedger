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

        // Watchers ko store krny k liye variables
        private var titleWatcher: TextWatcher? = null
        private var amountWatcher: TextWatcher? = null

        fun bind(item: PersonalExpenseUi) = with(b) {

            // 1. Purane Listeners Remove (Must)
            etPersonalTitle.removeTextChangedListener(titleWatcher)
            etPersonalAmount.removeTextChangedListener(amountWatcher)

            // 2. UI Update Logic (The Fix is Here)
            // Agar user is field me type kr rha hy (Focus hy), tu UI update mat kro.
            // Wo jo likh rha hy wo hi latest hy. ViewModel k pas data ja rha hy background me.

            if (!etPersonalTitle.hasFocus()) {
                etPersonalTitle.setTextIfDifferent(item.title)
            }

            if (!etPersonalAmount.hasFocus()) {
                etPersonalAmount.setTextIfDifferent(item.amount)
            }

            // 3. Re-attach Watchers

            titleWatcher = etPersonalTitle.doAfterTextChanged {
                if (etPersonalTitle.hasFocus()) {
                    onTitleChanged(item.id, it.toString())
                }
            }

            amountWatcher = etPersonalAmount.doAfterTextChanged {
                if (etPersonalAmount.hasFocus()) {
                    onAmountChanged(item.id, it.toString())
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