package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.miassolutions.milkledger.core.helper.handleZeroData
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.ui.BaseListAdapter
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
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
        tvDate.text = item.date.formattedDate()
        tvMilk.text = item.milkAmount.toRoundedStr()
        tvPrice.text = item.milkPrice.toRoundedStr()
        tvPayment.text = item.payment.toRoundedStr()

        tvBalance.text = numberFormat(item.balance)
        tvBalance.setTextColor(textColor(item.balance))

        if (item.notes.isNullOrEmpty()) {
            tvNotes.hide()
        } else {

            tvNotes.text = "Note: ${item.notes}"
        }

        // 1. Reset default appearance for rate and date
        tvDate.setTextColor(Color.BLACK)
        tvRate.hide()

        // 2. Use the new flag to show the alert only once
        if (item.isRateChangeStart) {
            // Use RED for the alert (or BLUE for consecutive if you keep that logic)
            tvDate.setTextColor(Color.RED)
            tvRate.show()
            tvRate.text = "Rate changed to: ${item.newRate.toRoundedStr()}" // Show the NEW rate
        }

        // 3. OPTIONAL: Keep the logic for highlighting today's entry if it's special
        val isToday = item.date == LocalDate.now()
        if (isToday && item.isRateChanged && !item.isRateChangeStart) {
            // You might still want to highlight today's entry even if the change started previously
            // This is application-specific visual preference.
            // Example: Highlight today's entry, even if the rate started yesterday
            // tvDate.setTextColor(Color.GREEN)
        }



    }

}