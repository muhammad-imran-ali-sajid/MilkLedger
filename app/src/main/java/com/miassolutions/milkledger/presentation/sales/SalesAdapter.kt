package com.miassolutions.milkledger.presentation.sales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.ItemSalesEntryBinding

class SalesEntryAdapter(
    onItemClick: ((SaleWithCustomer) -> Unit)? = null
) : BaseListAdapter<SaleWithCustomer, ItemSalesEntryBinding>(
    diffCallback = object : DiffUtil.ItemCallback<SaleWithCustomer>() {
        override fun areItemsTheSame(
            oldItem: SaleWithCustomer,
            newItem: SaleWithCustomer
        ): Boolean {
            return oldItem.customer.customerId == newItem.customer.customerId
        }

        override fun areContentsTheSame(
            oldItem: SaleWithCustomer,
            newItem: SaleWithCustomer
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

    override fun bind(binding: ItemSalesEntryBinding, item: SaleWithCustomer, position: Int) {
        binding.apply {
            tvCustomerName.text =
                item.customer.customerName  // You should replace this with actual customer name lookup
            tvVolume.text = item.sale.volume.toRoundedStr()
            tvDeduction.text = item.sale.deduction.toRoundedStr()
            tvTotal.text = item.sale.price.toRoundedStr()
            tvPrice.text = item.sale.price.toRoundedStr()
            tvPaid.text = item.sale.price.toRoundedStr() // Assuming full payment for simplicity
            tvBalance.text = "0" // Placeholder, compute if needed

            if (item.sale.notes.isNullOrBlank()) {
                tvNotes.hide()
            } else {
                tvNotes.show()
                tvNotes.text = "نوٹ: ${item.sale.notes}"
            }

        }
    }


}
