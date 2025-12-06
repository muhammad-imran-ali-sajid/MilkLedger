package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import com.miassolutions.milkledger.core.helper.handleZeroData
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.databinding.ItemSupplierDetailBinding
import java.time.LocalDate

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

        tvFat.text = handleZeroData(item.fat)
        tvLr.text = handleZeroData(item.lr)
        tvTs.text = handleZeroData(item.ts)
        tvDate.text = item.date.toDisplayFormat()
        tvMilk.text = item.milkAmount.toRoundedStr()
        tvPrice.text = item.milkPrice.toPriceStr()
        tvPayment.text = item.payment.toPriceStr()

        tvBalance.text = numberFormat(item.balance)
        tvBalance.setTextColor(textColor(item.balance))

        if (item.notes.isNullOrEmpty()) {
            tvNotes.hide()
        } else {

            tvNotes.text = "Note: ${item.notes}"
        }

        if (item.isRateChanged) {
            rateAlert.text = "Rate Change Alert (${item.rateUsed.toPriceStr()})"
            rateAlert.show()
            root.setCardBackgroundColor("#ccff00".toColorInt())
        } else {
            rateAlert.text = "Rate: (${item.rateUsed.toPriceStr()})"
            rateAlert.setTextColor("#000000".toColorInt())
            root.setCardBackgroundColor("#ffffff".toColorInt())
        }


    }

}