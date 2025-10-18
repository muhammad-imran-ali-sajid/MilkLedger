package com.miassolutions.milkledger.presentation.sales

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
    onItemClick: ((SaleWithCustomer) -> Unit)? = null,
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
    onItemClick = onItemClick,
    inflate = ItemSalesBinding::inflate
) {

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup): ItemSalesBinding {
        return ItemSalesBinding.inflate(inflater, parent, false)
    }

    override fun bind(binding: ItemSalesBinding, item: SaleWithCustomer, position: Int) {
        binding.apply {
            tvCustomerName.text =
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
                tvNotes.text = "نوٹ: ${item.sale.notes}"
            }

            // 👇 Long click on customer name only
            tvCustomerName.setOnLongClickListener {
                Log.d("SalesEntryAdapter", "Long click detected : ${item.customer.customerId}")
                navToDetailClick(item.customer.customerId, item.customer.customerName)
                true
            }

            // Optional: ensure it accepts long clicks
            tvCustomerName.isLongClickable = true
            tvCustomerName.isClickable = true

        }
    }


}
