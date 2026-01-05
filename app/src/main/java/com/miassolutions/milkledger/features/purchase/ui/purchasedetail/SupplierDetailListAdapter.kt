package com.miassolutions.milkledger.features.purchase.ui.purchasedetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemSupplierDetailBinding
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.helper.handleZeroData
import com.miassolutions.milkledger.utils.helper.numberFormat
import com.miassolutions.milkledger.utils.helper.textColor

class SupplierDetailListAdapter :
    androidx.recyclerview.widget.ListAdapter<SupplierDetailModel, SupplierDetailListAdapter.ViewHolder>(
        GenericDiffCallback(
            areItemsSame = { old, new -> old.date == new.date },
            areContentsSame = { old, new -> old == new }
        )
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupplierDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSupplierDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SupplierDetailModel) = with(binding) {

            tvFat.text = handleZeroData(item.fat)
            tvLr.text = handleZeroData(item.lr)
            tvTs.text = handleZeroData(item.ts)
            tvDate.text = item.date.toCompleteDateFormat()
            tvMilk.text = item.milkAmount.toMilkAmount()
            tvPrice.text = item.milkPrice.toPrice()
            tvPayment.text = item.payment.toPrice()

            tvBalance.text = numberFormat(item.balance)
            tvBalance.setTextColor(textColor(item.balance))

            if (item.notes.isNullOrEmpty()) {
                tvNotes.hide()
            } else {
                tvNotes.show()
                tvNotes.text = "Note: ${item.notes}"
            }

            applyRateAlert(item)
        }
    }

    // ✅ Helper method extracted
    private fun ItemSupplierDetailBinding.applyRateAlert(
        item: SupplierDetailModel
    ) {
        if (item.isRateChanged) {
            rateAlert.text = "RCA (${item.rateUsed.toPrice()})"
            rateAlert.show()
            root.setCardBackgroundColor("#ccff00".toColorInt())
        } else {
            rateAlert.text = "Rate: (${item.rateUsed.toPrice()})"
            rateAlert.setTextColor("#000000".toColorInt())
            root.setCardBackgroundColor("#ffffff".toColorInt())
        }
    }
}

