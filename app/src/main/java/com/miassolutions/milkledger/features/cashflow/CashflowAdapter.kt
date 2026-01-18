package com.miassolutions.milkledger.features.cashflow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.databinding.ItemCashFlowBinding
import com.miassolutions.milkledger.databinding.ItemSectionHeaderBinding
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class CashflowAdapter : ListAdapter<LedgerListItem, RecyclerView.ViewHolder>(DiffCallback) {

    // View Types define karen
    private val TYPE_HEADER = 0
    private val TYPE_ITEM = 1

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is LedgerListItem.Header -> TYPE_HEADER
            is LedgerListItem.Transaction -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = ItemSectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            HeaderViewHolder(binding)
        } else {
            val binding = ItemCashFlowBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            TransactionViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item as LedgerListItem.Header)
            is TransactionViewHolder -> holder.bind(item as LedgerListItem.Transaction)
        }
    }

    // --- View Holders ---

    inner class HeaderViewHolder(private val binding: ItemSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LedgerListItem.Header) {
            binding.tvHeaderTitle.text = item.title
        }
    }

    inner class TransactionViewHolder(private val binding: ItemCashFlowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemWrapper: LedgerListItem.Transaction) = with(binding) {
            val item = itemWrapper.data

            // 1. Date Formatting
            // Sirf Date aur Month dikhayen taake jagah kam lay (e.g., "12 Oct")
            tvDate.text = item.dateMillis.toLocalDate().toDisplayDate()

            // 2. Description
            tvDescription.text = item.note ?: getTypeLabel(item.type)

            // 3. Amount Logic (Single TextView) 🟢🔴
            val isCashIn = item.type == LedgerEntryType.CASH_RECEIVED

            if (isCashIn) {
                // 🟢 CASH IN (Green)
                val amount = item.credit // Cash In hamesha Credit column me hota hai
                tvAmount.text = "+ ${amount.toPrice()}"
                tvAmount.setTextColor(ContextCompat.getColor(root.context, R.color.milk_profit_color))
            } else {
                // 🔴 CASH OUT (Red)
                // Paid, Expense, Drawing sab Debit column me hotay hen
                val amount = item.debit
                tvAmount.text = "- ${amount.toPrice()}"
                tvAmount.setTextColor(ContextCompat.getColor(root.context, R.color.milk_expense_color))
            }
        }

        private fun getTypeLabel(type: LedgerEntryType): String {
            return when(type) {
                LedgerEntryType.CASH_RECEIVED -> "Received"
                LedgerEntryType.CASH_PAID -> "Payment"
                LedgerEntryType.BUSINESS_EXPENSE -> "Expense"
                LedgerEntryType.OWNER_DRAWING -> "Drawing"
                else -> "Transaction"
            }
        }
    }

    // --- Diff Util ---
    companion object DiffCallback : DiffUtil.ItemCallback<LedgerListItem>() {
        override fun areItemsTheSame(oldItem: LedgerListItem, newItem: LedgerListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LedgerListItem, newItem: LedgerListItem): Boolean {
            return oldItem == newItem
        }
    }
}