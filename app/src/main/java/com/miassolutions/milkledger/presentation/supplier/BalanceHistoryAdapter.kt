package com.miassolutions.milkledger.presentation.supplier

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemBalanceHitoryBinding

class BalanceHistoryAdapter(
    private var balanceHistoryList: List<BalanceHistory> = emptyList()
) : RecyclerView.Adapter<BalanceHistoryAdapter.BalanceHistoryViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BalanceHistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val b = ItemBalanceHitoryBinding.inflate(inflater, parent, false)
        return BalanceHistoryViewHolder(b)
    }

    override fun onBindViewHolder(
        holder: BalanceHistoryViewHolder,
        position: Int
    ) {
        val item = balanceHistoryList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = balanceHistoryList.size

    class BalanceHistoryViewHolder(private val binding: ItemBalanceHitoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BalanceHistory) {
            binding.tvDate.text = item.date.toString()
            binding.tvBalance.text = item.balance.toString()
        }
    }

    fun submitList(newList: List<BalanceHistory>) {
        balanceHistoryList = newList
        notifyDataSetChanged()
    }
}