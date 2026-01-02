package com.miassolutions.milkledger.features.sale.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.extensions.toRoundedStr
import com.miassolutions.milkledger.databinding.ItemSalesBinding
import com.miassolutions.milkledger.features.sale.domain.model.SaleUi


class SaleListAdapter(
    private val onEditClick: (String) -> Unit,
    private val onCustomerClick: (String, String) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onBalanceClick: (String, String) -> Unit
) : ListAdapter<SaleUi, SaleListAdapter.SaleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val binding = ItemSalesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SaleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SaleViewHolder(
        private val binding: ItemSalesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SaleUi) = with(binding) {

            // ---------------------------
            // Header
            // ---------------------------
            tvName.text = item.customerName

            btnEditForm.setOnClickListener {
                onEditClick(item.id)
            }

            btnCustomerDetail.setOnClickListener {
                onCustomerClick(item.customerId, item.customerName)
            }

            // ---------------------------
            // Milk Info
            // ---------------------------
            tvMilk.text = item.volume.toRoundedStr()
            tvDeduction.text = item.deduction.toRoundedStr()
            tvNetMilk.text = item.netMilk.toRoundedStr()

            // ---------------------------
            // Price / Payment
            // ---------------------------
            tvPrice.text = item.price.toPriceStr()
            tvPayment.text = item.paid.toPriceStr()

            // Receive date (optional)
            if (item.paid > 0 && item.paidAt != null) {
                tvReceiveDate.visibility = View.VISIBLE
                tvReceiveDate.text = "(${item.paidAt.toDisplayFormat()})"
            } else {
                tvReceiveDate.visibility = View.GONE
            }

            // ---------------------------
            // Balance
            // ---------------------------
            tvBalance.text = item.accumulatedBalance.toPriceStr()

            btnBalance.setOnClickListener {
                onBalanceClick(item.customerId, item.customerName)
            }

            // ---------------------------
            // Notes
            // ---------------------------
            if (!item.notes.isNullOrBlank()) {
                tvNotes.visibility = View.VISIBLE
                tvNotes.text = item.notes
            } else {
                tvNotes.visibility = View.GONE
            }

            // ---------------------------
            // Long click → delete
            // ---------------------------
            root.setOnLongClickListener {
                onDeleteClick(item.id)
                true
            }
        }
    }

    companion object {

        private val DiffCallback = object : DiffUtil.ItemCallback<SaleUi>() {

            override fun areItemsTheSame(
                oldItem: SaleUi,
                newItem: SaleUi
            ): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: SaleUi,
                newItem: SaleUi
            ): Boolean =
                oldItem == newItem
        }
    }
}
