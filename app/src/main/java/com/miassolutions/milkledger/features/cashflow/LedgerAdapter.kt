package com.miassolutions.milkledger.features.cashflow


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.databinding.ItemLedgerHistoryBinding
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class LedgerAdapter : ListAdapter<FinancialLedgerEntity, LedgerAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLedgerHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemLedgerHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FinancialLedgerEntity) = with(binding) {

            // 1. Date & Description
            tvDate.text = item.dateMillis.toLocalDate().toCompleteDateFormat()
            tvType.text = item.note ?: "Transaction"

            // 2. Amount Logic (In vs Out)
            if (item.credit > 0) {
                // 🟢 CASH IN (Received)
                tvCredit.text = "+ ${item.credit.toPrice()}"
                tvCredit.setTextColor(ContextCompat.getColor(root.context, R.color.green_700))

//                // Optional: Icon change kar skte hen
//                ivIcon.setImageResource(R.drawable.ic_arrow_downward) // Arrow Down = Jeb me aya
//                ivIcon.setColorFilter(ContextCompat.getColor(root.context, R.color.green_700))
//                viewIndicator.setBackgroundColor(ContextCompat.getColor(root.context, R.color.green_700))

            } else {
                // 🔴 CASH OUT (Paid/Withdraw)
                tvDebit.text = "- ${item.debit.toPrice()}"
                tvDebit.setTextColor(ContextCompat.getColor(root.context, R.color.red))

                // Optional: Icon
//                ivIcon.setImageResource(R.drawable.ic_arrow_upward) // Arrow Up = Jeb se gya
//                ivIcon.setColorFilter(ContextCompat.getColor(root.context, R.color.red_700))
//                viewIndicator.setBackgroundColor(ContextCompat.getColor(root.context, R.color.red_700))
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<FinancialLedgerEntity>() {
        override fun areItemsTheSame(oldItem: FinancialLedgerEntity, newItem: FinancialLedgerEntity): Boolean {
            return oldItem.ledgerId == newItem.ledgerId
        }

        override fun areContentsTheSame(oldItem: FinancialLedgerEntity, newItem: FinancialLedgerEntity): Boolean {
            return oldItem == newItem
        }
    }
}