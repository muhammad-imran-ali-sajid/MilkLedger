package com.miassolutions.milkledger.features.sale.ui.saledetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemCustomerDetailBinding
import com.miassolutions.milkledger.core.ui.GenericDiffCallback
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.helper.numberFormat
import com.miassolutions.milkledger.utils.helper.textColor

class CustomerDetailListAdapter :
    ListAdapter<CustomerDetailModel, CustomerDetailListAdapter.ViewHolder>(
        GenericDiffCallback(
            areItemsSame = { old, new -> old.date == new.date },
            areContentsSame = { old, new -> old == new }
        )
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCustomerDetailBinding.inflate(
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
        private val binding: ItemCustomerDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CustomerDetailModel) = with(binding) {

            tvDate.text = item.date.toDisplayFormat()
            tvMilk.text = item.milkAmount.toString()
            tvDeduction.text = item.deduction.toString()
            tvNetMilk.text = item.netMilk.toString()
            tvPrice.text = item.milkPrice.toString()
            tvPayment.text = item.payment.toString()

            if (item.rateChanged) {
                rateAlert.text = "RCA (${item.rateUsed.toPriceStr()})"
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
        }
    }
}
