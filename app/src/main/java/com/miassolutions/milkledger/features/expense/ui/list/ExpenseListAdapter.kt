package com.miassolutions.milkledger.features.expense.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemExpensesBinding
import com.miassolutions.milkledger.features.expense.data.local.ExpenseEntity
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toPriceStr

class ExpensesAdapter(
    private val onClick: (ExpenseEntity) -> Unit,
    private val onLongClick: (ExpenseEntity) -> Unit
) : ListAdapter<ExpenseEntity, ExpensesAdapter.ExpenseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding =
            ItemExpensesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemExpensesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExpenseEntity) = with(binding) {
            tvExpenseTitle.text = item.expenseTitle
            tvExpenseAmount.text = item.expenseAmount.toPriceStr()

            if (item.note.isNullOrEmpty()) {
                tvExpenseNote.hide()
                divider.hide()
            } else {
                tvExpenseNote.show()
                divider.show()
                tvExpenseNote.text = "Note: ${item.note}"
            }


            root.setOnClickListener { onClick(item) }
            root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpenseEntity>() {
        override fun areItemsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean =
            oldItem.expenseId == newItem.expenseId

        override fun areContentsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean =
            oldItem == newItem
    }
}
