package com.miassolutions.milkledger.features.milk.balancehistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemBalanceHitoryBinding
import com.miassolutions.milkledger.features.milk.model.BalanceHistoryUi
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColorRupee
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toRupeesStr

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
                // Date
                tvDate.text = item.date.toDisplayDate()

                // Net Change (Optional info)
                val sign = if (item.netChange > 0) "+" else "" // Minus khud aa jata hai
                tvNetChange.text = "Day Total: $sign${item.netChange.toRupeesStr()}"

                // ✅ MAIN: Closing Balance
                tvClosingBalance.setBalanceWithColorRupee(item.closingBalance)
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
        return oldItem.date == newItem.date && oldItem.date == newItem.date
    }

    override fun areContentsTheSame(
        oldItem: BalanceHistoryUi,
        newItem: BalanceHistoryUi
    ): Boolean {
        return oldItem == newItem
    }
}