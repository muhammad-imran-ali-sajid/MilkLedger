package com.miassolutions.milkledger.features.milk.balancehistory


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemCustomerHistoryBinding
import com.miassolutions.milkledger.features.milk.model.CustomerHistoryUi
import com.miassolutions.milkledger.features.milk.model.HistoryType
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class HistoryAdapter : ListAdapter<CustomerHistoryUi, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemCustomerHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemCustomerHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CustomerHistoryUi) {
            binding.apply {
                // 1. Date Format (e.g., 05 Jan 2024)
                tvDate.text = item.date.toDisplayDate()

                // 2. Description (e.g., Cash Received)
                tvDescription.text = item.description

                // 3. Amount Format (e.g., Rs. 5,000)
                tvAmount.text = item.amount.toPrice(showCurrency = true)

                // 4. Color Logic 🎨
                val context = root.context
                val colorRes = when (item.type) {
                    HistoryType.CREDIT -> R.color.green // Payment (Green)
                    HistoryType.DEBIT -> R.color.red    // Sale/Udhaar (Red)
                }


                // Colors.xml wala rang uthayen
                tvAmount.setTextColor(ContextCompat.getColor(context, colorRes))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CustomerHistoryUi>() {
        override fun areItemsTheSame(oldItem: CustomerHistoryUi, newItem: CustomerHistoryUi): Boolean {
            // Chunke unique ID nahi hai UI model me, hum date aur desc check kr lety hen
            // ya agar ledgerId entity se pass ho rahi ho to best hai.
            return oldItem.date == newItem.date && oldItem.amount == newItem.amount
        }

        override fun areContentsTheSame(oldItem: CustomerHistoryUi, newItem: CustomerHistoryUi): Boolean {
            return oldItem == newItem
        }
    }
}