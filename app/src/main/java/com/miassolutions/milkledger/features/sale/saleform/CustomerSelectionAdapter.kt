package com.miassolutions.milkledger.features.sale.saleform


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemCustomerSelectionBinding

class CustomerSelectionAdapter(
    private val onCustomerClick: (CustomerDropDownUiModel) -> Unit
) : ListAdapter<CustomerDropDownUiModel, CustomerSelectionAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemCustomerSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomerDropDownUiModel) {
            binding.tvCustomerName.text = item.account.name
            binding.ivStatus.isVisible = item.isEntryDoneToday

            binding.root.setOnClickListener {
                onCustomerClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemCustomerSelectionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CustomerDropDownUiModel>() {
        override fun areItemsTheSame(oldItem: CustomerDropDownUiModel, newItem: CustomerDropDownUiModel) =
            oldItem.account.accountId == newItem.account.accountId

        override fun areContentsTheSame(oldItem: CustomerDropDownUiModel, newItem: CustomerDropDownUiModel) =
            oldItem == newItem
    }
}