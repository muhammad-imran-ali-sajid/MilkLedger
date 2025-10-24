package com.miassolutions.milkledger.presentation.supplier.purchase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.helper.numberFormat
import com.miassolutions.milkledger.core.helper.textColor
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding
import kotlin.math.roundToInt

class PurchaseAdapter(
    private val onEditClick: (PurchaseWithSupplier) -> Unit,
    private val onItemDetailClick: (PurchaseWithSupplier) -> Unit
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

            if (item.purchase.fat == 0.0) {
                tvFat.text = "--"
            } else {
                tvFat.text = item.purchase.fat.toRoundedStr("%.2f")
            }


            if (item.purchase.lr == 0.0) {
                tvLr.text = "--"
            } else {
                tvLr.text = item.purchase.lr.toRoundedStr("%.2f")
            }


            if (item.purchase.ts == 0.0) {
                tvTs.text = "--"
            } else {
                tvTs.text = item.purchase.ts.toRoundedStr("%.2f")
            }


            if (item.purchase.notes.isNullOrBlank()) {
                tvNotes.hide()
                divider.hide()
            } else {
                tvNotes.show()
                divider.show()
                tvNotes.text = "Note: ${item.purchase.notes}"
            }
            tvPrice.text = item.purchase.milkPrice.roundToInt().toString()
            tvPaid.text = item.purchase.payment.roundToInt().toString()




            tvBalance.text = numberFormat(item.purchase.balance)
            tvBalance.setTextColor(textColor(item.purchase.balance))


            btnEditForm.setOnClickListener { onEditClick(item) }
            btnSupplierDetail.setOnClickListener { onItemDetailClick(item) }
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
