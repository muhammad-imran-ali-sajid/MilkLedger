package com.miassolutions.milkledger.presentation.supplier.purchase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.helper.handleZeroData
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding

class PurchaseAdapter(
    private val onEditClick: (PurchaseWithSupplier) -> Unit,
    private val onItemDetailClick: (PurchaseWithSupplier) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onBalanceClick: (String, String) -> Unit
) : ListAdapter<PurchaseWithSupplier, PurchaseAdapter.ViewHolder>(DiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPurchaseBinding.inflate(
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
        private val binding: ItemPurchaseBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseWithSupplier) = with(binding) {
            tvName.text = item.supplier.supplierName



            tvVolume.text = item.purchase.milkAmount.toRoundedStr()

            tvFat.text = handleZeroData(item.purchase.fat)
            tvLr.text = handleZeroData(item.purchase.lr)
            tvTs.text = handleZeroData(item.purchase.ts)


            if (item.purchase.notes.isNullOrBlank()) {
                tvNotes.hide()
                divider.hide()
            } else {
                tvNotes.show()
                divider.show()
                tvNotes.text = "Note: ${item.purchase.notes}"
            }
            tvPrice.text = item.purchase.milkPrice.toPriceStr()
            tvPaid.text = item.purchase.payment.toPriceStr()

            val balance = item.purchase.payment - item.purchase.milkPrice

            tvBalance.text = numberFormat(balance)
            tvBalance.setTextColor(textColor(balance))

            btnBalance.setOnLongClickListener {
                onBalanceClick(
                    item.supplier.supplierId,
                    item.supplier.supplierName
                ); true
            }


            btnEditForm.setOnClickListener { onEditClick(item) }
            btnSupplierDetail.setOnClickListener { onItemDetailClick(item) }

            tvName.setOnLongClickListener {
                onDeleteClick(item.purchase.purchaseId)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PurchaseWithSupplier>() {
        override fun areItemsTheSame(oldItem: PurchaseWithSupplier, newItem: PurchaseWithSupplier) =
            oldItem.purchase.purchaseId == newItem.purchase.purchaseId

        override fun areContentsTheSame(
            oldItem: PurchaseWithSupplier,
            newItem: PurchaseWithSupplier
        ) =
            oldItem == newItem
    }
}
