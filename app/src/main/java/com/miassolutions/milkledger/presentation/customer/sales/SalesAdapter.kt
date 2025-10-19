package com.miassolutions.milkledger.presentation.customer.sales

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.ItemSalesBinding
import kotlin.math.truncate

class SalesEntryAdapter(
    private val onEditClick: (SaleWithCustomer) -> Unit,
    private val navToDetailClick: (String, String) -> Unit
) : BaseListAdapter<SaleWithCustomer, ItemSalesBinding>(
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

    inflate = ItemSalesBinding::inflate
) {

    var isEditable = false

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemSalesBinding {
        return ItemSalesBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemSalesBinding, item: SaleWithCustomer, position: Int) {
        binding.apply {
            tvName.text =
                item.customer.customerName  // You should replace this with actual customer name lookup
            tvMilk.text = item.sale.volume.toRoundedStr()
            tvDeduction.text = item.sale.deduction.toRoundedStr()
            tvNetMilk.text = item.sale.netMilk.toRoundedStr()
            tvPrice.text = item.sale.price.toRoundedStr()
            tvPayment.text = item.sale.price.toRoundedStr() // Assuming full payment for simplicity
            tvBalance.text = "0" // Placeholder, compute if needed

            if (item.sale.notes.isNullOrBlank()) {
                tvNotes.hide()
            } else {
                tvNotes.show()
                tvNotes.text = "Note: ${item.sale.notes}"
            }


            btnCustomerDetail.setOnClickListener {
                navToDetailClick(item.customer.customerId, item.customer.customerName)

            }

            btnEditForm.setOnClickListener {
                onEditClick(item)
            }


        }
    }


}
