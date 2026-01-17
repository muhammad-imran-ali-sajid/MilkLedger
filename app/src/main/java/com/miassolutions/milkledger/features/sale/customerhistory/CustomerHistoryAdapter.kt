package com.miassolutions.milkledger.features.sale.customerhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemCustomerDetailBinding // CardView wala XML
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toFormattedMilk
import com.miassolutions.milkledger.utils.extensions.toPrice

class CustomerHistoryAdapter(
) : ListAdapter<MilkSaleUiModel, CustomerHistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemCustomerDetailBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemCustomerDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(item: MilkSaleUiModel) = with(binding) {
            // 1. Date
            tvDate.text = item.dateMillis.toLocalDate().toCompleteDateFormat()

            // 2. Milk Details
            tvMilk.text = item.quantity.toFormattedMilk()
            tvDeduction.text = item.deduction.toString()
            tvNetMilk.text = item.netQuantity.toFormattedMilk()


            rateAlert.isVisible = false

            // 4. Financials
            tvPrice.text = item.totalAmount.toPrice()

            val isRateChanged = item.previousRate != null && item.previousRate != item.rate

            if (isRateChanged) {
                rateAlert.show()
                rateAlert.text =
                    "Rate Alert: ${item.previousRate} -> ${item.rate}"  //Show Alert: Rate Changed from
                root.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, R.color.md_theme_primaryContainer)
                )
            } else {
                root.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, R.color.md_theme_surfaceVariant)
                )
            }

            // Payment Logic
            if (item.paymentReceived > 0) {
                tvPayment.text = item.paymentReceived.toPrice()
//                // Agar Payment Date sale date se mukhtalif hai to show karein
                 val payDate = item.paymentDateMillis?.toLocalDate()
                 if (payDate != null && payDate != item.dateMillis.toLocalDate()) {
                     tvPaymentDate.show()
                     tvPaymentDate.text = "(${payDate.toDisplayDate()})"
                 }
            } else {
                tvPayment.text = "-" // Ya 0
            }


            // 5. Running Balance
            tvBalance.setBalanceWithColor(item.currentBalance)

            // 6. Note
            if (!item.note.isNullOrBlank()) {
                tvNotes.text = "Note: ${item.note}"
                tvNotes.isVisible = true
            } else {
                tvNotes.isVisible = false
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MilkSaleUiModel>() {
        override fun areItemsTheSame(oldItem: MilkSaleUiModel, newItem: MilkSaleUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MilkSaleUiModel, newItem: MilkSaleUiModel): Boolean {
            return oldItem == newItem
        }
    }
}