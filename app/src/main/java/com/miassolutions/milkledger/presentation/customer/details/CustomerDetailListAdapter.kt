package com.miassolutions.milkledger.presentation.customer.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
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
        tvDate.text = item.date.toDisplayFormat()
        tvMilk.text = item.milkAmount.toString()
        tvDeduction.text = item.deduction.toString()
        tvNetMilk.text = item.netMilk.toString()
        tvPrice.text = item.milkPrice.toString()
        tvPayment.text = item.payment.toString()

        if (item.rateChanged) {
            rateAlert.text = "Rate Change Alert (${item.rateUsed.toPriceStr()})"
            rateAlert.show()
            root.setCardBackgroundColor("#ccff00".toColorInt())
        } else {
            rateAlert.text = "Rate: (${item.rateUsed.toPriceStr()})"
            rateAlert.setTextColor("#000000".toColorInt())
            root.setCardBackgroundColor("#ffffff".toColorInt())
        }



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