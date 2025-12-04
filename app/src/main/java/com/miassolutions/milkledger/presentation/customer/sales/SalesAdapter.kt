package com.miassolutions.milkledger.presentation.customer.sales

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toDisplayDate
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import com.miassolutions.milkledger.databinding.ItemSalesBinding
import com.miassolutions.milkledger.domain.model.Sale
import kotlin.math.truncate

class SalesEntryAdapter(
    private val onEditClick: (Sale) -> Unit,
    private val navToDetailClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onBalanceClick: (String, String) -> Unit,
) : BaseListAdapter<Sale, ItemSalesBinding>(
    diffCallback = object : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(
            oldItem: Sale,
            newItem: Sale
        ): Boolean {
            return oldItem.customerId == newItem.customerId
        }

        override fun areContentsTheSame(
            oldItem: Sale,
            newItem: Sale
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

    override fun bind(binding: ItemSalesBinding, item: Sale, position: Int) {
        binding.apply {


            if (item.receivedDate != null) {
                tvReceiveDate.show()
                tvReceiveDate.text = "(${item.receivedDate.toDisplayDate()})"
            } else {
                tvReceiveDate.hide()
            }

            val payment = item.received.toPriceStr()

            tvName.text =
                item.name
            tvMilk.text = item.volume.toRoundedStr()
            tvDeduction.text = item.deduction.toRoundedStr()
            tvNetMilk.text = item.netVolume.toRoundedStr()
            tvPrice.text = item.price.toPriceStr()
            tvPayment.text = payment


            val balance = item.balance

            tvBalance.text = numberFormat(balance)
            tvBalance.setTextColor(textColor(balance))


            btnBalance.setOnClickListener {
                onBalanceClick(item.customerId, item.name)
            }

            if (item.notes.isNullOrBlank()) {
                tvNotes.hide()
            } else {
                tvNotes.show()
                tvNotes.text = "Note: ${item.notes}"
            }


            btnCustomerDetail.setOnClickListener {
                navToDetailClick(item.customerId, item.name)

            }

            btnEditForm.setOnClickListener {
                onEditClick(item)
            }

            tvName.setOnLongClickListener {
                onDeleteClick(item.saleId)
                true
            }


        }
    }


}
