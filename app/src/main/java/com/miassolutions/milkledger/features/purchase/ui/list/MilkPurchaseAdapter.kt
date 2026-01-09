package com.miassolutions.milkledger.features.purchase.ui.list


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class MilkPurchaseAdapter(
    private val onEditClick: (String) -> Unit,
    private val onHistoryClick: (String, String) -> Unit
) : ListAdapter<MilkPurchaseUiModel, MilkPurchaseAdapter.PurchaseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding = ItemPurchaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PurchaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PurchaseViewHolder(private val binding: ItemPurchaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MilkPurchaseUiModel) = with(binding) {
            tvName.text = item.supplierName

            // Measurements
            tvVolume.text = String.format("%.1f", item.volume)
            tvFat.text = String.format("%.1f", item.fat)
            tvLr.text = String.format("%.1f", item.lr)
            tvTs.text = String.format("%.2f", item.ts)

            // Financials
            tvPrice.text = item.totalAmount.toPrice()
            tvPayment.text = item.paymentMade.toPrice()

            // Balance logic
            tvBalance.setBalanceWithColor(item.currentBalance)

            // Payment Date Logic (Show only if different from Sale Date)
            val payDate = item.paymentDate
            val saleDate = item.dateMillis.toLocalDate()

            if (payDate != null && !payDate.isEqual(saleDate) && item.paymentMade > 0) {
                tvPaymentDate.isVisible = true
                // Format: (20/11)
                tvPaymentDate.text = "(${payDate.dayOfMonth}/${payDate.monthValue})"
            } else {
                tvPaymentDate.isVisible = false
            }

            // Note
            if (!item.note.isNullOrBlank()) {
                tvNotes.isVisible = true
                tvNotes.text = "Note: ${item.note}"
            } else {
                tvNotes.isVisible = false
            }

            // Click Listeners
            btnEditForm.setOnClickListener { onEditClick(item.id) }
            btnBalance.setOnClickListener { onHistoryClick(item.supplierId, item.supplierName) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MilkPurchaseUiModel>() {
        override fun areItemsTheSame(oldItem: MilkPurchaseUiModel, newItem: MilkPurchaseUiModel) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MilkPurchaseUiModel, newItem: MilkPurchaseUiModel) =
            oldItem == newItem
    }
}