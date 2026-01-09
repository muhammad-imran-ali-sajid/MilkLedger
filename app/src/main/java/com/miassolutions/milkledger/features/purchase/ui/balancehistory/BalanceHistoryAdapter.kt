package com.miassolutions.milkledger.features.purchase.ui.balancehistory


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.databinding.ItemLedgerHistoryBinding
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class BalanceHistoryAdapter : ListAdapter<FinancialLedgerEntity, BalanceHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLedgerHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemLedgerHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FinancialLedgerEntity) = with(binding) {
            tvDate.text = item.dateMillis.toLocalDate().toDisplayDate()

            // Type formatting
            tvType.text = when(item.type) {
                LedgerEntryType.MILK_SALE -> "Milk Sale"
                LedgerEntryType.CASH_RECEIVED -> "Cash Rec."
                LedgerEntryType.MILK_PURCHASE -> "Milk Purch."
                LedgerEntryType.CASH_PAID -> "Cash Paid"
                else -> "Other"
            }

            // Amounts
            if (item.debit > 0) {
                tvDebit.text = item.debit.toPrice()
                tvDebit.isVisible = true
            } else {
                tvDebit.text = "-"
                // tvDebit.isVisible = false // Optional: keep alignment
            }

            if (item.credit > 0) {
                tvCredit.text = item.credit.toPrice()
                tvCredit.isVisible = true
            } else {
                tvCredit.text = "-"
            }

            // Note Icon
            ivNote.isVisible = !item.note.isNullOrBlank()
            if (!item.note.isNullOrBlank()) {
                root.setOnClickListener {
                    // Optional: Show full note on click via Toast/Dialog
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FinancialLedgerEntity>() {
        override fun areItemsTheSame(oldItem: FinancialLedgerEntity, newItem: FinancialLedgerEntity) = oldItem.ledgerId == newItem.ledgerId
        override fun areContentsTheSame(oldItem: FinancialLedgerEntity, newItem: FinancialLedgerEntity) = oldItem == newItem
    }
}