package com.miassolutions.milkledger.presentation.expenses

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.databinding.ItemExpensesBinding

class ExpensesAdapter(
    private val onClick: (ExpensesEntity) -> Unit,
    private val onLongClick: (ExpensesEntity) -> Unit
) : ListAdapter<ExpensesEntity, ExpensesAdapter.ExpenseViewHolder>(DiffCallback()) {

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

        fun bind(item: ExpensesEntity) {
            binding.tvExpenseTitle.text = item.expenseTitle
            binding.tvExpenseAmount.text = item.expenseAmount.toString()
            binding.tvExpenseNote.text = item.expenseNote

            binding.root.setOnClickListener { onClick(item) }
            binding.root.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpensesEntity>() {
        override fun areItemsTheSame(oldItem: ExpensesEntity, newItem: ExpensesEntity): Boolean =
            oldItem.expenseId == newItem.expenseId

        override fun areContentsTheSame(oldItem: ExpensesEntity, newItem: ExpensesEntity): Boolean =
            oldItem == newItem
    }
}
