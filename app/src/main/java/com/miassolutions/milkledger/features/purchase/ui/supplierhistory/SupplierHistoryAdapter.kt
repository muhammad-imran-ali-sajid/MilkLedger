package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemSupplierHistoryBinding
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class SupplierHistoryAdapter(
) : ListAdapter<MilkPurchaseUiModel, SupplierHistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemSupplierHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSupplierHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {



        fun bind(item: MilkPurchaseUiModel) = with(binding) {
            // Date
            tvDate.text = item.dateMillis.toLocalDate().toCompleteDateFormat()

            // Rate Alert (Example Logic: Hide if standard)
            rateAlert.isVisible = false

            // Measurements
            tvMilk.text = String.format("%.1f", item.volume)
            tvFat.text = String.format("%.1f", item.fat)
            tvLr.text = String.format("%.1f", item.lr)
            tvTs.text = String.format("%.2f", item.ts)

            // Financials
            tvPrice.text = item.totalAmount.toPrice()

            val isRateChanged = item.previousRate != null && item.previousRate != item.rate

            if (isRateChanged) {
                rateAlert.show()
                rateAlert.text =
                    "Rate: ${item.previousRate} -> ${item.rate}"  //Show Alert: Rate Changed from
                root.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, R.color.orange_200)
                )
            }

            // Payment Logic
            if (item.paymentMade > 0) {
                tvPayment.text = item.paymentMade.toPrice()
                tvPayment.isVisible = true

                // Optional: Show Date if different
                val payDate = item.paymentDate
                val saleDate = item.dateMillis.toLocalDate()
                if (payDate != null && !payDate.isEqual(saleDate)) {
                    tvPaymentDate.isVisible = true
                    tvPaymentDate.text = "(${payDate.dayOfMonth}/${payDate.monthValue})"
                } else {
                    tvPaymentDate.isVisible = false
                }
            } else {
                tvPayment.text = "-"
                tvPaymentDate.isVisible = false
            }

            // Running Balance
            tvBalance.setBalanceWithColor(item.currentBalance)

            // Note
            if (!item.note.isNullOrBlank()) {
                tvNotes.text = "Note: ${item.note} (${item.paymentDate?.toDisplayDate()})"
                tvNotes.isVisible = true
            } else {
                tvNotes.isVisible = true
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MilkPurchaseUiModel>() {
        override fun areItemsTheSame(oldItem: MilkPurchaseUiModel, newItem: MilkPurchaseUiModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: MilkPurchaseUiModel,
            newItem: MilkPurchaseUiModel
        ) = oldItem == newItem
    }
}