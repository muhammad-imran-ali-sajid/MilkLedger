package com.miassolutions.milkledger.features.expense.ui.list


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemExpensesBinding
import com.miassolutions.milkledger.features.expense.domain.Expense
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(
    private val onItemClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpensesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemExpensesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Expense) {
            binding.apply {
                tvExpenseTitle.text = item.title

                // Paisa -> Rupees Conversion logic
                val rupees = item.amount / 100.0
                tvExpenseAmount.text = "Rs. ${String.format("%.2f", rupees)}"

                // Note Handling
                if (item.note.isNullOrBlank()) {
                    tvExpenseNote.visibility = View.GONE
                    divider.visibility = View.GONE
                } else {
                    tvExpenseNote.visibility = View.VISIBLE
                    divider.visibility = View.VISIBLE
                    tvExpenseNote.text = "Note: ${item.note}"
                }

                // Click Listener
                root.setOnClickListener { onItemClick(item) }
            }
        }
    }

    class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense) =
            oldItem.expenseId == newItem.expenseId

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense) =
            oldItem == newItem
    }
}
