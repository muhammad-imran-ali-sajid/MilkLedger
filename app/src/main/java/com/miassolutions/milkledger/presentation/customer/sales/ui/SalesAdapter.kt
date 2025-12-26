package com.miassolutions.milkledger.presentation.customer.sales.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.extensions.hide
import com.miassolutions.milkledger.core.extensions.show
import com.miassolutions.milkledger.core.extensions.toDisplayDate
import com.miassolutions.milkledger.core.extensions.toPriceStr
import com.miassolutions.milkledger.core.extensions.toRoundedStr
import com.miassolutions.milkledger.databinding.ItemSalesBinding
import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.presentation.customer.sales.model.SaleUi

class SalesEntryAdapter(
    private val onEditClick: (Sale) -> Unit,
    private val navToDetailClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onBalanceClick: (String, String) -> Unit,
) : BaseListAdapter<SaleUi, ItemSalesBinding>(
    diffCallback = object : DiffUtil.ItemCallback<SaleUi>() {
        override fun areItemsTheSame(
            oldItem: SaleUi,
            newItem: SaleUi
        ): Boolean {
            return oldItem.data.customerId == newItem.data.customerId
        }

        override fun areContentsTheSame(
            oldItem: SaleUi,
            newItem: SaleUi
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

    override fun bind(binding: ItemSalesBinding, item: SaleUi, position: Int) {
        binding.apply {
            item.data.apply {

//                if (receivedDate != null) {
//                    tvReceiveDate.show()
//                    tvReceiveDate.text = "(${receivedDate.toDisplayDate()})"
//                } else {
//                    tvReceiveDate.hide()
//                }
//
//                val payment = received.toPriceStr()
//
//                tvName.text = name
//                tvMilk.text = volume.toRoundedStr()
//                tvDeduction.text = deduction.toRoundedStr()
//                tvNetMilk.text = netVolume.toRoundedStr()
//                tvPrice.text = price.toPriceStr()
//                tvPayment.text = payment


                val balance = item.accumulatedBalance

                tvBalance.text = numberFormat(balance)
                tvBalance.setTextColor(textColor(balance))


//                btnBalance.setOnLongClickListener {
//                    onBalanceClick(customerId, name)
//                    true
//                }

                if (notes.isNullOrBlank()) {
                    tvNotes.hide()
                } else {
                    tvNotes.show()
                    tvNotes.text = "Note: ${notes}"
                }


//                btnCustomerDetail.setOnClickListener {
//                    navToDetailClick(customerId, name)
//
//                }
//
//                btnEditForm.setOnClickListener {
//                    onEditClick(item.data)
//                }
//
//                tvName.setOnLongClickListener {
//                    onDeleteClick(item.data.saleId)
//                    true
//                }


            }
        }

    }
}
