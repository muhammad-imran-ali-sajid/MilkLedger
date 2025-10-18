package com.miassolutions.milkledger.core.ui.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemCategoryChipBinding

class CategoryAdapter(
    private val onCategorySelected: (String?) -> Unit
) : ListAdapter<String, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    private var selectedCategory: String? = null

    fun setSelected(category: String?) {
        selectedCategory = category
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String) {
            binding.chipCategory.text = category
            binding.chipCategory.isChecked = (category == selectedCategory)

            binding.chipCategory.setOnClickListener {
                selectedCategory =
                    if (selectedCategory == category) null else category
                onCategorySelected(selectedCategory)
                notifyDataSetChanged()
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    }
}
