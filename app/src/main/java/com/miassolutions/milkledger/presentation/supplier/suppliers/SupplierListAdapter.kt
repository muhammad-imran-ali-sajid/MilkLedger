package com.miassolutions.milkledger.presentation.supplier.suppliers

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemSupplierBinding
import com.miassolutions.milkledger.domain.model.Supplier

class SupplierListAdapter(
    private val onEditClick: (Supplier) -> Boolean = { false }
) : ListAdapter<Supplier, SupplierListAdapter.SupplierViewHolder>(SupplierDiffCallback()) {

    inner class SupplierViewHolder(
        private val binding: ItemSupplierBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(supplier: Supplier, position: Int) {
            val b = binding

            // --- Basic binding ---
            b.tvPosition.text = supplier.sortOrder.toString()
            b.tvSupplierName.text = supplier.name
            b.tvSupplierRate.text = "${"%.2f".format(supplier.rate)}"
            b.tvAdvanceAmount.text = "${"%.2f".format(supplier.advanceAmount)}"

            // --- Expansion handling ---
            b.layoutExpandable.visibility =
                if (supplier.isExpanded) android.view.View.VISIBLE else android.view.View.GONE
            b.imgArrow.rotation = if (supplier.isExpanded) 180f else 0f

            // --- Click to expand/collapse ---
            b.root.setOnClickListener {
                val updatedSupplier = supplier.copy(isExpanded = !supplier.isExpanded)
                val newList = currentList.toMutableList()
                newList[position] = updatedSupplier

                // Animate smooth expand/collapse
                TransitionManager.beginDelayedTransition(b.root as ViewGroup, AutoTransition())
                submitList(newList)
            }

            // --- Long click listener (edit or extra action) ---
            b.root.setOnLongClickListener {
                onEditClick(supplier)
            }
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
        holder.bind(getItem(position), position)
    }
}

class SupplierDiffCallback : DiffUtil.ItemCallback<Supplier>() {
    override fun areItemsTheSame(oldItem: Supplier, newItem: Supplier): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Supplier, newItem: Supplier): Boolean =
        oldItem == newItem
}
