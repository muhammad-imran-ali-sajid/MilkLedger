package com.miassolutions.milkledger.presentation.purchase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding
import kotlin.math.roundToInt

class PurchaseAdapter(
    private val onItemClick: (PurchaseWithSupplier) -> Unit
) : ListAdapter<PurchaseWithSupplier, PurchaseAdapter.ViewHolder>(DiffCallback()) {

    var isEditable = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPurchaseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPurchaseBinding,
        private val onItemClick: (PurchaseWithSupplier) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseWithSupplier) {
            binding.tvName.text = item.supplier.supplierName
            binding.tvFat.text = "%.2f".format(item.purchase.fat)
            binding.tvNotes.text = item.purchase.notes
            binding.tvPrice.text = item.purchase.price.roundToInt().toString()
            binding.tvPaid.text = item.purchase.paid.roundToInt().toString()
            binding.tvBalance.text = item.purchase.balance.roundToInt().toString()
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PurchaseWithSupplier>() {
        override fun areItemsTheSame(oldItem: PurchaseWithSupplier, newItem: PurchaseWithSupplier) =
            oldItem.purchase.purchaseId == newItem.purchase.purchaseId

        override fun areContentsTheSame(
            oldItem: PurchaseWithSupplier,
            newItem: PurchaseWithSupplier
        ) =
            oldItem == newItem
    }
}
