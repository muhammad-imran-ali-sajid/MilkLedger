package com.miassolutions.milkledger.presentation.profit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.util.toDisplayDate
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.ItemProfitBinding
import com.miassolutions.milkledger.domain.model.Profit

class ProfitAdapter(
    private val onItemClick: (ProfitListModel) -> Unit,
    private val onItemLongClick: (ProfitListModel) -> Unit
) : RecyclerView.Adapter<ProfitAdapter.ProfitViewHolder>() {

    private val list: MutableList<ProfitListModel> = mutableListOf()

    inner class ProfitViewHolder(private val binding: ItemProfitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProfitListModel) = with(binding) {
            tvDate.text = item.date
            tvProfit.text = item.profit.toPriceStr()
            tvProfitReceived.text = item.profitReceived.toPriceStr()
            tvBalance.text = (item.profit - item.profitReceived).toPriceStr()

            root.setOnClickListener { onItemClick(item) }
            root.setOnLongClickListener { onItemLongClick(item); true }

        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfitViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemProfitBinding.inflate(inflater, parent, false)
        return ProfitViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ProfitViewHolder,
        position: Int
    ) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun submitList(newList: List<ProfitListModel>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


}