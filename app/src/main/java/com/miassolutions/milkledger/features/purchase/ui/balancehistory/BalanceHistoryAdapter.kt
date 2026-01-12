package com.miassolutions.milkledger.features.purchase.ui.balancehistory


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.databinding.ItemLedgerHistoryBinding
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class BalanceHistoryAdapter : ListAdapter<DailyLedgerUiModel, BalanceHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLedgerHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemLedgerHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DailyLedgerUiModel) = with(binding) {
            tvDate.text = item.dateMillis.toLocalDate().toDisplayDate()

            // Ab Type ki jagah Description/Summary ayegi
            tvType.text = item.description

            // Debit Display
            if (item.totalDebit > 0) {
                tvDebit.text = item.totalDebit.toPrice()
                tvDebit.isVisible = true
            } else {
                tvDebit.text = "--"
            }

            // Credit Display
            if (item.totalCredit > 0) {
                tvCredit.text = item.totalCredit.toPrice()
                tvCredit.isVisible = true
            } else {
                tvCredit.text = "--"
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DailyLedgerUiModel>() {
        override fun areItemsTheSame(oldItem: DailyLedgerUiModel, newItem: DailyLedgerUiModel) = oldItem.dateMillis == newItem.dateMillis
        override fun areContentsTheSame(oldItem: DailyLedgerUiModel, newItem: DailyLedgerUiModel) = oldItem == newItem
    }
}