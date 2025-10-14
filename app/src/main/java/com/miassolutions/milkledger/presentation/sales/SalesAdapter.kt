package com.miassolutions.milkledger.presentation.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.databinding.ItemSalesEntryBinding

class SalesEntryAdapter(
    onItemClick: ((SalesEntryEntity) -> Unit)? = null
) : BaseListAdapter<SalesEntryEntity, ItemSalesEntryBinding>(
    diffCallback = object : DiffUtil.ItemCallback<SalesEntryEntity>() {
        override fun areItemsTheSame(
            oldItem: SalesEntryEntity,
            newItem: SalesEntryEntity
        ): Boolean {
            return oldItem.saleId == newItem.saleId
        }

        override fun areContentsTheSame(
            oldItem: SalesEntryEntity,
            newItem: SalesEntryEntity
        ): Boolean {
            return oldItem == newItem
        }
    },
    onItemClick = onItemClick,
    inflate = ItemSalesEntryBinding::inflate
) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemSalesEntryBinding {
        return ItemSalesEntryBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemSalesEntryBinding, item: SalesEntryEntity, position: Int) {
        binding.apply {
            tvCustomerName.text =
                item.customerId  // You should replace this with actual customer name lookup
            tvVolume.text = item.volume.toString()
            tvDeduction.text = item.deduction.toString()
            tvTotal.text = item.price.toString()
            tvPrice.text = item.price.toString()
            tvPaid.text = item.price.toString() // Assuming full payment for simplicity
            tvBalance.text = "0" // Placeholder, compute if needed
            tvNotes.text =
                if (item.notes.isNullOrBlank()) "نوٹ: کچھ نہیں لکھا گیا" else "نوٹ: ${item.notes}"
        }
    }
}
