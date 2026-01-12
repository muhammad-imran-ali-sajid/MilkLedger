package com.miassolutions.milkledger.features.cashflow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.databinding.ItemLedgerHistoryBinding
import com.miassolutions.milkledger.databinding.ItemSectionHeaderBinding
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
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
            val binding = ItemLedgerHistoryBinding.inflate(
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

    inner class TransactionViewHolder(private val binding: ItemLedgerHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemWrapper: LedgerListItem.Transaction) = with(binding) {
            val item = itemWrapper.data

            tvDate.text = item.dateMillis.toLocalDate().toCompleteDateFormat()
            tvType.text = item.note ?: getTypeLabel(item.type)

            // Logic for Cash In / Cash Out colors
            // Note: Cashflow screen hai, is liye hum sirf Cash movements dekh rahe hain

            // Green Logic (Paisa Aya)
            if (item.type == LedgerEntryType.CASH_RECEIVED) {
                tvCredit.text = "+ ${item.credit.toPrice()}"
                tvCredit.setTextColor(ContextCompat.getColor(root.context, R.color.green_700))
                tvDebit.text = "" // Hide Debit field

                // Optional Icon Logic
                // ivIcon.setImageResource(R.drawable.ic_arrow_downward)

            }
            // Red Logic (Paisa Gaya)
            else {
                tvDebit.text = "- ${item.debit.toPrice()}"
                tvDebit.setTextColor(ContextCompat.getColor(root.context, R.color.red))
                tvCredit.text = "" // Hide Credit field

                // ivIcon.setImageResource(R.drawable.ic_arrow_upward)
            }
        }

        private fun getTypeLabel(type: LedgerEntryType): String {
            return when (type) {
                LedgerEntryType.CASH_RECEIVED -> "Received from Customer"
                LedgerEntryType.CASH_PAID -> "Paid to Supplier"
                LedgerEntryType.BUSINESS_EXPENSE -> "Expense"
                LedgerEntryType.OWNER_DRAWING -> "Owner Drawing"
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