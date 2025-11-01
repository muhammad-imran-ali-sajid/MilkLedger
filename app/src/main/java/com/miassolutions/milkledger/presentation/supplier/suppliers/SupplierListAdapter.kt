package com.miassolutions.milkledger.presentation.supplier.suppliers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.ItemSupplierBinding
import com.miassolutions.milkledger.domain.model.Supplier

/**
 * Supplier ListAdapter with internal ViewHolder.
 * Supports item clicks, drag-and-drop, and swipe actions.
 */
class SupplierListAdapter(
    private val onItemLongClick: ((Supplier) -> Boolean)? = null,
) : ListAdapter<Supplier, SupplierListAdapter.SupplierViewHolder>(SupplierDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val binding = ItemSupplierBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder is defined inside the adapter
     */
    inner class SupplierViewHolder(
        private val binding: ItemSupplierBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(supplier: Supplier) {
            binding.tvSupplierName.text = supplier.name
            binding.tvSupplierRate.text = supplier.rate.toRoundedStr("%.2f")
            binding.tvSort.text = supplier.sortOrder.toString()
            // Item click listener
            binding.root.setOnLongClickListener {
                onItemLongClick?.invoke(supplier)
                true
            }
        }
    }

    /**
     * DiffCallback for ListAdapter
     */
    class SupplierDiffCallback : DiffUtil.ItemCallback<Supplier>() {
        override fun areItemsTheSame(oldItem: Supplier, newItem: Supplier): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Supplier, newItem: Supplier): Boolean {
            return oldItem == newItem
        }
    }
}
