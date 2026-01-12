package com.miassolutions.milkledger.features.purchase.ui.form

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemSupplierSelectionBinding
import com.miassolutions.milkledger.features.purchase.model.SupplierDropDownUiModel

class SupplierSelectionAdapter(
    private val onSupplierClick: (SupplierDropDownUiModel) -> Unit
) : ListAdapter<SupplierDropDownUiModel, SupplierSelectionAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemSupplierSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SupplierDropDownUiModel) {
            binding.tvSupplierName.text = item.account.name

            // Show check mark if entry is done today
            binding.ivStatus.isVisible = item.isEntryDoneToday


            if (item.isEntryDoneToday) {
                binding.tvSupplierName.alpha = 0.5f // Text dhundla (dim) karein
                binding.root.isEnabled = true // Clickable rakhen taake Toast dikha saken
            } else {
                binding.tvSupplierName.alpha = 1.0f // Normal Text
                binding.root.isEnabled = true
            }

            binding.root.setOnClickListener {
                onSupplierClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSupplierSelectionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SupplierDropDownUiModel>() {
        override fun areItemsTheSame(oldItem: SupplierDropDownUiModel, newItem: SupplierDropDownUiModel) =
            oldItem.account.accountId == newItem.account.accountId

        override fun areContentsTheSame(oldItem: SupplierDropDownUiModel, newItem: SupplierDropDownUiModel) =
            oldItem == newItem
    }
}