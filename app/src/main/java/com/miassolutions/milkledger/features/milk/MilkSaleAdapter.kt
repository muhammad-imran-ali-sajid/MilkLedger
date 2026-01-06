package com.miassolutions.milkledger.features.milk


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemSalesBinding
import com.miassolutions.milkledger.features.sale.domain.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.toMilkAmount
import com.miassolutions.milkledger.utils.extensions.toPrice

class MilkSaleAdapter(
    private val onEditClick: (MilkSaleUiModel) -> Unit,
    private val onDetailClick: (MilkSaleUiModel) -> Unit,
    private val onBalanceClick: (customerId: String, customerName: String) -> Unit
) : ListAdapter<MilkSaleUiModel, MilkSaleAdapter.SaleViewHolder>(DiffCallback()) {

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

                // Payment Info (Agar payment feature linked ho to yahan show karein)
                if (item.paymentReceived > 0) {
                    tvPayment.text = item.paymentReceived.toPrice()
                    tvReceiveDate.isVisible = false // Date toggle logic if needed
                } else {
                    tvPayment.text = "-"
                    tvReceiveDate.isVisible = false
                }

                // Balance (Optional: List me balance show krna heavy query hoti hai)
                tvBalance.text = "-"

                // Note
                if (item.note.isNullOrBlank()) {
                    tvNotes.isVisible = false
                } else {
                    tvNotes.isVisible = true
                    tvNotes.text = "Note: ${item.note}"
                }

                // Clicks
                btnEditForm.setOnClickListener { onEditClick(item) }
                btnCustomerDetail.setOnClickListener { onDetailClick(item) }
                binding.btnBalance.setOnClickListener {
                    onBalanceClick(item.customerId, item.customerName)
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