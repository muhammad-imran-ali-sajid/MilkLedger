package com.miassolutions.milkledger.presentation.supplier.purchase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.utils.helper.handleZeroData
import com.miassolutions.milkledger.utils.helper.numberFormat
import com.miassolutions.milkledger.utils.helper.textColor
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.utils.extensions.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding

class PurchaseAdapter(
    private val onEditClick: (PurchaseWithSupplier) -> Unit,
    private val onItemDetailClick: (PurchaseWithSupplier) -> Unit,
    private val onDeleteClick: (String) -> Unit,
    private val onBalanceClick: (String, String) -> Unit
) : ListAdapter<PurchaseUi, PurchaseAdapter.ViewHolder>(DiffCallback()) {


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

        fun bind(item: PurchaseUi) = with(binding) {

            tvName.text = item.data.supplier.supplierName



            tvVolume.text = item.data.purchase.milkAmount.toRoundedStr()

            tvFat.text = handleZeroData(item.data.purchase.fat)
            tvLr.text = handleZeroData(item.data.purchase.lr)
            tvTs.text = handleZeroData(item.data.purchase.ts)


            if (item.data.purchase.notes.isNullOrBlank()) {
                tvNotes.hide()
                divider.hide()
            } else {
                tvNotes.show()
                divider.show()
                tvNotes.text = "Note: ${item.data.purchase.notes}"
            }
            tvPrice.text = item.data.purchase.milkPrice.toPriceStr()
            tvPaid.text = item.data.purchase.payment.toPriceStr()

            val balance = item.accumulatedBalance

            tvBalance.text = numberFormat(balance)
            tvBalance.setTextColor(textColor(balance))

            btnBalance.setOnLongClickListener {
                onBalanceClick(
                    item.data.supplier.supplierId,
                    item.data.supplier.supplierName
                ); true
            }


            btnEditForm.setOnClickListener { onEditClick(item.data) }
            btnSupplierDetail.setOnClickListener { onItemDetailClick(item.data) }

            tvName.setOnLongClickListener {
                onDeleteClick(item.data.purchase.purchaseId)
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PurchaseUi>() {
        override fun areItemsTheSame(oldItem: PurchaseUi, newItem: PurchaseUi) =
            oldItem.data.purchase.purchaseId == newItem.data.purchase.purchaseId

        override fun areContentsTheSame(
            oldItem: PurchaseUi,
            newItem: PurchaseUi
        ) =
            oldItem == newItem
    }
}
