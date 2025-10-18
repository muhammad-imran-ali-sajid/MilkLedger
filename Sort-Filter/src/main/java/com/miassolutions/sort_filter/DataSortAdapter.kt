package com.miassolutions.sort_filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.sort_filter.databinding.ItemSortFilterBinding


class DataSortAdapter<T>(
    private var items: List<T>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<DataSortAdapter<T>.VH>() {

    inner class VH(val binding: ItemSortFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: T) {
            when (item) {
                is FilterOption -> {
                    binding.tvTitle.text = item.title
                    binding.checkbox.isChecked = item.isSelected
                    binding.root.setOnClickListener { onClick(item.id) }
                }
                is SortOption -> {
                    binding.tvTitle.text = item.title
                    binding.checkbox.isChecked = item.isSelected
                    binding.root.setOnClickListener { onClick(item.id) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSortFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    fun submitList(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }
}