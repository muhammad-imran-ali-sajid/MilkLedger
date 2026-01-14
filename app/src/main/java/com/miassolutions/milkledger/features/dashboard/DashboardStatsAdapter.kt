package com.miassolutions.milkledger.features.dashboard


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemDashboardStatBinding
import com.miassolutions.milkledger.features.dashboard.model.DashboardStat

class DashboardStatsAdapter : ListAdapter<DashboardStat, DashboardStatsAdapter.StatViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatViewHolder {
        val binding = ItemDashboardStatBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return StatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StatViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class StatViewHolder(private val binding: ItemDashboardStatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardStat) {
            binding.tvTitle.text = item.title
            binding.tvValue.text = item.value

            // Color Logic: Agar color dia hua hai to wo lagayen, warna Default Green Theme
            if (item.valueColor != null) {
                binding.tvValue.setTextColor(ContextCompat.getColor(binding.root.context, item.valueColor))
            } else {
                // Default Theme Color (Green) - Reset zaroori hai RecyclerView mein
//                val defaultColor = com.google.android.material.R.attr.colorPrimary
//                // Note: Better to get color from attributes properly, but for now assuming style handles default
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DashboardStat>() {
        override fun areItemsTheSame(oldItem: DashboardStat, newItem: DashboardStat) = oldItem.title == newItem.title
        override fun areContentsTheSame(oldItem: DashboardStat, newItem: DashboardStat) = oldItem == newItem
    }
}