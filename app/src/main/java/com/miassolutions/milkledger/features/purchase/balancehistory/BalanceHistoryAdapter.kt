package com.miassolutions.milkledger.features.purchase.balancehistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.helper.numberFormat
import com.miassolutions.milkledger.utils.helper.textColor
import com.miassolutions.milkledger.databinding.ItemBalanceHitoryBinding

class BalanceHistoryAdapter(
    private var balanceHistoryList: List<BalanceHistory> = emptyList(),
    private val onItemClick: ((BalanceHistory) -> Unit)? = null
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

    inner class BalanceHistoryViewHolder(private val binding: ItemBalanceHitoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BalanceHistory) {
            binding.tvDate.text = item.dateMillis.toLocalDate().toDisplayDate()

            val balance = item.balance
//            binding.tvBalance.text = numberFormat(balance)
//            binding.tvBalance.setTextColor(textColor(balance))

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }

        }
    }

    fun submitList(newList: List<BalanceHistory>) {
        balanceHistoryList = newList
        notifyDataSetChanged()
    }
}