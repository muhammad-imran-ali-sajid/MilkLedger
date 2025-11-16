package com.miassolutions.milkledger.presentation.customer.details

import android.view.LayoutInflater
import android.view.ViewGroup
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.databinding.ItemCustomerDetailBinding

class CustomerDetailListAdapter :
    BaseListAdapter<CustomerDetailModel, ItemCustomerDetailBinding>(
        inflate = ItemCustomerDetailBinding::inflate,
        diffCallback = GenericDiffCallback(
            areItemsSame = { old, new -> old.date == new.date },
            areContentsSame = { old, new -> old == new }
        )) {
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemCustomerDetailBinding {
        return ItemCustomerDetailBinding.inflate(inflater, parent, false)
    }

    override fun bind(
        binding: ItemCustomerDetailBinding,
        item: CustomerDetailModel,
        position: Int
    ) = with(binding) {
        tvDate.text = item.date.toString()
        tvMilk.text = item.milkAmount.toString()
        tvDeduction.text = item.deduction.toString()
        tvNetMilk.text = item.netMilk.toString()
        tvPrice.text = item.milkPrice.toString()
        tvPayment.text = item.payment.toString()


        val balance = item.milkPrice - item.payment

        tvBalance.text = numberFormat(balance)
        tvBalance.setTextColor(textColor(balance))

        if (item.notes.isNullOrBlank()) {
            tvNotes.hide()
        } else {
            tvNotes.show()
            tvNotes.text = "Note: ${item.notes}"
        }

        tvNotes.text = item.notes
    }

}