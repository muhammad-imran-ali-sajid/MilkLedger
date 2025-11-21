package com.miassolutions.milkledger.presentation.profit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.util.toDisplayDate
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.ItemProfitBinding
import com.miassolutions.milkledger.domain.model.Profit

class ProfitAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProfitAdapter.ProfitViewHolder>() {

    private val list: MutableList<Profit> = mutableListOf()

    inner class ProfitViewHolder(private val binding: ItemProfitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Profit) = with(binding) {
            tvDate.text = item.receivedDate.toDisplayDate()
            tvProfit.text = item.receivedProfit.toPriceStr()

            root.setOnClickListener { onItemClick(item.profitId) }

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

    fun submitList(newList: List<Profit>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }


}