package com.miassolutions.milkledger.features.sale.salelist


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemSalesBinding
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.extensions.toPrice

class MilkSaleListAdapter(
    private val onEditClick: (saleId: String) -> Unit,
    private val onDeleteClick: (saleId: String) -> Unit,
    private val onDetailClick: (MilkSaleUiModel) -> Unit,
    private val onBalanceClick: (customerId: String, customerName: String) -> Unit
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
                tvMilk.text = item.quantity.toMilkAmount()
                tvDeduction.text = item.deduction.toMilkAmount()
                tvNetMilk.text = item.netQuantity.toMilkAmount()

                // Financial Info
                tvPrice.text = item.totalAmount.toPrice() // Price (Bill)

                tvReceiveDate.text = item.paymentDate?.toDisplayDate()

                // Payment Info (Agar payment feature linked ho to yahan show karein)
                if (item.paymentReceived > 0) {
                    tvPayment.text = item.paymentReceived.toPrice()
                    tvReceiveDate.isVisible = true // Date toggle logic if needed
                } else {
                    tvPayment.text = "-"
                    tvReceiveDate.isVisible = false
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
                    onBalanceClick(item.customerId, item.customerName)
                }


                tvName.setOnLongClickListener {
                    onDeleteClick(item.id)
                    true
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