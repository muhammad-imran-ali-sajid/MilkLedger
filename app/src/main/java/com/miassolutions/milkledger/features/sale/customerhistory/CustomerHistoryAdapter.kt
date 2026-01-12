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
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.extensions.toPrice

class CustomerHistoryAdapter(
//    private val onItemClick: (MilkSaleUiModel) -> Unit
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

        init {
//            binding.root.setOnClickListener {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onItemClick(getItem(position))
//                }
//            }
        }

        fun bind(item: MilkSaleUiModel) = with(binding) {
            // 1. Date
            tvDate.text = item.dateMillis.toLocalDate().toCompleteDateFormat()

            // 2. Milk Details
            tvMilk.text = item.quantity.toMilkAmount()
            tvDeduction.text = item.deduction.toString()
            tvNetMilk.text = item.netQuantity.toMilkAmount()

            // Deduction UI Visibility
            val hasDeduction = item.deduction > 0
            // (Agar aapke XML me Deduction ka poora column layout/linear layout hai to usay hide kr skty hen)
            // e.g. layoutDeduction.isVisible = hasDeduction

            // 3. Rate Alert
            // Logic: Rate change show krna ya hide krna (Assuming standard rate logic)
            // Filhal hardcoded ya hidden rakhein jab tak logic final na ho
            rateAlert.isVisible = false

            // 4. Financials
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