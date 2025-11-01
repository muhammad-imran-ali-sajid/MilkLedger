package com.miassolutions.milkledger.presentation.supplier.suppliers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.helper.recyclerviewhelper.ReorderableAdapter
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.ItemSupplierBinding
import com.miassolutions.milkledger.domain.model.Supplier

/**
 * Supplier ListAdapter with internal ViewHolder.
 * Supports item clicks, drag-and-drop, and swipe actions.
 */
class SupplierListAdapter(
    private val onItemClick: ((Supplier) -> Unit)? = null,
    private val onItemDelete: ((Supplier) -> Unit)? = null
) : ListAdapter<Supplier, SupplierListAdapter.SupplierViewHolder>(SupplierDiffCallback()),
    ReorderableAdapter<Supplier> {

    // Adapter’s current list for ReorderableAdapter
    override val items: List<Supplier>
        get() = super.getCurrentList()

    /** Called when an item is moved via drag-and-drop */
    override fun onItemMove(fromPosition: Int, toPosition: Int): List<Supplier> {
        val mutableList = items.toMutableList()
        val item = mutableList.removeAt(fromPosition)
        mutableList.add(toPosition, item)
        submitList(mutableList)
        return mutableList
    }

    /** Called when an item is swiped */
    override fun onItemSwiped(position: Int, direction: Int) {
        if (position !in items.indices) return

        val item = items[position]

        if (direction == ItemTouchHelper.LEFT) {
            val mutableList = items.toMutableList()
            mutableList.removeAt(position)
            submitList(mutableList) // temporary visual update
            onItemDelete?.invoke(item)
        } else if (direction == ItemTouchHelper.RIGHT) {
            // Optional: mark as favorite locally
            val mutableList = items.toMutableList()
//            mutableList[position] = item.copy(isFavorite = true)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val binding = ItemSupplierBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SupplierViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        holder.bind(items[position])
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

            // Item click listener
            binding.root.setOnClickListener {
                onItemClick?.invoke(supplier)
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
