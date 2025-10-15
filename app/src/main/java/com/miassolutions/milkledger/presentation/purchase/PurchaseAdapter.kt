package com.miassolutions.milkledger.presentation.purchase

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.util.hide
import com.miassolutions.milkledger.core.util.show
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding
import kotlin.math.roundToInt

class PurchaseAdapter(
    private val onItemClick: (PurchaseWithSupplier) -> Unit
) : ListAdapter<PurchaseWithSupplier, PurchaseAdapter.ViewHolder>(DiffCallback()) {

    var isEditable = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPurchaseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemPurchaseBinding,
        private val onItemClick: (PurchaseWithSupplier) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseWithSupplier) = with(binding) {
            tvName.text = item.supplier.supplierName

            tvVolume.text = item.purchase.volume.toRoundedStr()

            if (item.purchase.fat == 0.0) {
                tvFat.text = "NA"
            } else {
                tvFat.text = item.purchase.fat.toRoundedStr()
            }


            if (item.purchase.lr == 0.0) {
                tvLr.text = "NA"
            } else {
                tvLr.text = item.purchase.lr.toRoundedStr()
            }


            if (item.purchase.ts == 0.0) {
                tvTs.text = "NA"
            } else {
                tvTs.text = item.purchase.ts.toRoundedStr()
            }


            if (item.purchase.notes.isNullOrBlank()) {
                tvNotes.hide()
            } else {
                tvNotes.show()
                tvNotes.text = "نوٹ: ${item.purchase.notes}"
            }
            tvPrice.text = item.purchase.price.roundToInt().toString()
            tvPaid.text = item.purchase.paid.roundToInt().toString()


            // Change color based on balance
            val balance = item.purchase.balance
            val color = when {
                balance < 0 -> Color.RED
                balance == 0.0 -> "#000000".toColorInt()
                else -> "#4CAF50".toColorInt() // Material green 500
            }
            // Format balance text with + sign if positive
            val balanceText = when {
                balance > 0 -> "+${balance.roundToInt()}"
                else -> balance.roundToInt().toString()
            }

            tvBalance.text = balanceText
            tvBalance.setTextColor(color)

            if (bindingAdapterPosition % 2 == 0) {
                root.setCardBackgroundColor("#dedede".toColorInt())
            } else {
                root.setCardBackgroundColor("#ffffff".toColorInt())
            }
            root.setOnClickListener { onItemClick(item) }
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
