package com.miassolutions.milkledger.features.milk.balancehistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemBalanceHitoryBinding
import com.miassolutions.milkledger.features.customer.domain.model.BalanceHistoryUi
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.extensions.toSignedBalance

class BalanceHistoryAdapter :
    ListAdapter<BalanceHistoryUi, BalanceHistoryAdapter.VH>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding =
            ItemBalanceHitoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(val binding: ItemBalanceHitoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BalanceHistoryUi) {
            binding.apply {
                // Date format: "05 Jan"
                tvDate.text =
                    item.date.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM"))
                tvDesc.text = item.description

                // Transaction Amount (+/-)
                val sign = if (item.isDebit) "+" else "-"
                tvAmount.text = "$sign ${item.amount.toPrice()}" // toPrice() extension Rs wala

                // Color Logic for Transaction
                val color = if (item.isDebit) R.color.red else R.color.green
                tvAmount.setTextColor(root.context.getColor(color))

                // Running Balance
                tvRunningBal.text = item.runningBalance.toSignedBalance() // With Sign (+/-)
            }
        }
    }


}

class DiffCallback : DiffUtil.ItemCallback<BalanceHistoryUi>() {
    override fun areItemsTheSame(
        oldItem: BalanceHistoryUi,
        newItem: BalanceHistoryUi
    ): Boolean {
        // Chunke unique ID nahi hai UI model me, hum date aur desc check kr lety hen
        // ya agar ledgerId entity se pass ho rahi ho to best hai.
        return oldItem.date == newItem.date && oldItem.amount == newItem.amount
    }

    override fun areContentsTheSame(
        oldItem: BalanceHistoryUi,
        newItem: BalanceHistoryUi
    ): Boolean {
        return oldItem == newItem
    }
}