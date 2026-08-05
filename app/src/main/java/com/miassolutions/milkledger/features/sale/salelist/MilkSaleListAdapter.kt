package com.miassolutions.milkledger.features.sale.salelist


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.miassolutions.milkledger.databinding.DialogPaymentInfoBinding
import com.miassolutions.milkledger.databinding.ItemSalesBinding
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toFormattedMilk
import com.miassolutions.milkledger.utils.extensions.toPrice

class MilkSaleListAdapter(
    private val onEditClick: (saleId: String) -> Unit,
    private val onDetailClick: (MilkSaleUiModel) -> Unit,
    private val onBalanceClick: (customerId: String, customerName: String, dateMillis: Long) -> Unit
) : ListAdapter<MilkSaleUiModel, MilkSaleListAdapter.SaleViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSalesBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SaleViewHolder(private val binding: ItemSalesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MilkSaleUiModel) {
            binding.apply {
                tvName.text = item.customerName

                // Volume Info
                tvMilk.text = item.quantity.toFormattedMilk()
                tvDeduction.text = item.deduction.toFormattedMilk()
                tvNetMilk.text = item.netQuantity.toFormattedMilk()

                // Financial Info
                tvPrice.text = item.totalAmount.toPrice() // Price (Bill)


                // Payment info
                if (item.paymentReceived > 0) {
                    tvPayment.text = item.paymentReceived.toPrice()
//
//                    tilPayment.setOnClickListener {
//                        val context = it.context
//                        val inflater = LayoutInflater.from(context)
//                        val binding = DialogPaymentInfoBinding.inflate(inflater)
//
//                        binding.tvMessage.text = "Rcv Date: ${item.paymentDate?.toDisplayDate()}"
//
//                        MaterialAlertDialogBuilder(context)
//                            .setView(binding.root)
//                            .setPositiveButton("OK", null)
//                            .show()
//                    }

                } else {
                    tvPayment.text = "-"
                    tvPayment.setOnClickListener(null)
                }




                tvBalance.setBalanceWithColor(item.currentBalance)

                // Note
                if (item.note.isNullOrBlank()) {
                    tvNotes.isVisible = false
                } else {
                    tvNotes.isVisible = true
                    tvNotes.text = "Note: ${item.note}"
                }

                // Clicks
                btnEditForm.setOnClickListener { onEditClick(item.id) }
                btnCustomerDetail.setOnClickListener { onDetailClick(item) }
                btnBalance.setOnClickListener {
                    onBalanceClick(item.customerId, item.customerName, item.dateMillis)
                }

            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MilkSaleUiModel>() {
        override fun areItemsTheSame(oldItem: MilkSaleUiModel, newItem: MilkSaleUiModel) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MilkSaleUiModel, newItem: MilkSaleUiModel) =
            oldItem == newItem
    }
}