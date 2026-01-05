package com.miassolutions.milkledger.features.sale.ui.balancehistory

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.databinding.ItemBalanceHitoryBinding
import com.miassolutions.milkledger.features.sale.domain.model.BalanceHistoryItem

class BalanceHistoryAdapter :
    ListAdapter<BalanceHistoryItem, BalanceHistoryAdapter.VH>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemBalanceHitoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(
        private val binding: ItemBalanceHitoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BalanceHistoryItem) = with(binding) {

            tvDate.text = item.date.toCompleteDateFormat()

            tvChange.text =
                if (item.change >= 0)
                    "+${item.change.toPrice()}"
                else
                    item.change.toPrice()

            tvChange.setTextColor(
                if (item.change >= 0)
                    Color.parseColor("#2E7D32") // green
                else
                    Color.parseColor("#C62828") // red
            )

            tvBalance.text = item.balanceAfter.toPrice()
        }
    }

    companion object {
        val Diff = object : DiffUtil.ItemCallback<BalanceHistoryItem>() {
            override fun areItemsTheSame(
                old: BalanceHistoryItem,
                new: BalanceHistoryItem
            ) = old.date == new.date && old.change == new.change

            override fun areContentsTheSame(
                old: BalanceHistoryItem,
                new: BalanceHistoryItem
            ) = old == new
        }
    }
}
