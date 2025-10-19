package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.ItemSupplierDetailBinding

class SupplierDetailListAdapter :
    BaseListAdapter<SupplierDetailModel, ItemSupplierDetailBinding>(
        inflate = ItemSupplierDetailBinding::inflate,
        diffCallback = GenericDiffCallback(
            areItemsSame = { old, new -> old.date == new.date },
            areContentsSame = { old, new -> old == new }
        )) {
    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup
    ): ItemSupplierDetailBinding {
        return ItemSupplierDetailBinding.inflate(inflater, parent, false)
    }

    override fun bind(
        binding: ItemSupplierDetailBinding,
        item: SupplierDetailModel,
        position: Int
    ) = with(binding) {


        if (item.fat == 0.0) {
            tvFat.text = "--"
        } else {
            tvFat.text = item.fat.toRoundedStr()
        }


        if (item.lr == 0.0) {
            tvLr.text = "--"
        } else {
            tvLr.text = item.lr.toRoundedStr()
        }


        if (item.ts == 0.0) {
            tvTs.text = "--"
        } else {
            tvTs.text = item.ts.toRoundedStr()
        }


        tvDate.text = item.date.formattedDate()
        tvMilk.text = item.milkAmount.toRoundedStr()
        tvPrice.text = item.milkPrice.toRoundedStr()
        tvPayment.text = item.payment.toRoundedStr()
        tvBalance.text = item.balance.toRoundedStr()

        if (item.notes.isNullOrEmpty()) {
            tvNotes.hide()
        } else {

            tvNotes.text = "Note: ${item.notes}"
        }
    }

}