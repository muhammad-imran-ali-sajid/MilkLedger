package com.miassolutions.milkledger.features.purchase.ui.list


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.databinding.DialogPaymentInfoBinding
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toFormattedMilk
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class MilkPurchaseAdapter(
    private val onEditClick: (String) -> Unit,
    private val onBalanceHistoryClick: (supplierId: String, supplierName: String, dateMillis: Long) -> Unit,
    private val onSupplierHistoryClick: (supplierId: String, supplierName: String) -> Unit
) : ListAdapter<MilkPurchaseUiModel, MilkPurchaseAdapter.PurchaseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding =
            ItemPurchaseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
            tvVolume.text = item.volume.toFormattedMilk()
            tvFat.text = item.fat.toFormattedMilk()
            tvLr.text = item.lr.toFormattedMilk()
            tvTs.text =  item.ts.toFormattedMilk()

            // Financials
            tvPrice.text = item.totalAmount.toPrice()
            tvPayment.text = item.paymentMade.toPrice()

            // Balance logic
            tvBalance.setBalanceWithColor(item.currentBalance)

            // Payment info
            if (item.paymentMade > 0) {
                tvPayment.text = item.paymentMade.toPrice()

                tilPayment.setOnClickListener {
                    val context = it.context
                    val inflater = LayoutInflater.from(context)
                    val binding = DialogPaymentInfoBinding.inflate(inflater)

                    binding.tvMessage.text =
                        "Payment Date: ${item.paymentDate?.toDisplayDate() ?: "Not available"}"

                    MaterialAlertDialogBuilder(context)
                        .setView(binding.root)
                        .setPositiveButton("OK", null)
                        .show()
                }

            } else {
                tvPayment.text = "-"
                tvPayment.setOnClickListener(null)
            }


            // Note
            if (!item.note.isNullOrBlank()) {
                tvNotes.isVisible = true
                tvNotes.text = "Note: ${item.note}"
            } else {
                tvNotes.isVisible = false
            }

            btnSupplierHistory.setOnClickListener {
                onSupplierHistoryClick(
                    item.supplierId,
                    item.supplierName
                )
            }
            // Click Listeners
            btnEditForm.setOnClickListener { onEditClick(item.id) }
            btnBalance.setOnClickListener {
                onBalanceHistoryClick(
                    item.supplierId,
                    item.supplierName,
                    item.dateMillis
                )
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MilkPurchaseUiModel>() {
        override fun areItemsTheSame(oldItem: MilkPurchaseUiModel, newItem: MilkPurchaseUiModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: MilkPurchaseUiModel,
            newItem: MilkPurchaseUiModel
        ) =
            oldItem == newItem
    }
}