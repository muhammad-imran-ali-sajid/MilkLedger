package com.miassolutions.milkledger.presentation.purchase


import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding

class PurchaseAdapter(
    private val onSupplierClick: (String) -> Unit,
    private val onVolumeChanged: (String, Double) -> Unit,
    private val onFatChanged: (String, Double) -> Unit,
    private val onLrChanged: (String, Double) -> Unit,
    private val onNotesChanged: (String, String) -> Unit
) : ListAdapter<PurchaseWithSupplier, PurchaseAdapter.PurchaseViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<PurchaseWithSupplier>() {
        override fun areItemsTheSame(
            oldItem: PurchaseWithSupplier,
            newItem: PurchaseWithSupplier
        ) = oldItem.purchase.purchaseId == newItem.purchase.purchaseId

        override fun areContentsTheSame(
            oldItem: PurchaseWithSupplier,
            newItem: PurchaseWithSupplier
        ) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding = ItemPurchaseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PurchaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PurchaseViewHolder(
        private val binding: ItemPurchaseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseWithSupplier) = with(binding) {
            tvSupplierName.text = item.supplier.supplierName
            etVolume.setText(item.purchase.volume.toString())
            etFat.setText(item.purchase.fat.toString())
            etLr.setText(item.purchase.lr.toString())
            etNotes.setText(item.purchase.notes ?: "")
            tvPrice.text = "Rs. ${"%.2f".format(item.purchase.price)}"
            tvTs.text = "TS: ${"%.2f".format(item.purchase.ts)}"

            tvSupplierName.setOnClickListener {
                onSupplierClick(item.supplier.supplierId)
            }

            addTextWatcher(etVolume) { text ->
                text.toDoubleOrNull()?.let { onVolumeChanged(item.purchase.purchaseId, it) }
            }
            addTextWatcher(etFat) { text ->
                text.toDoubleOrNull()?.let { onFatChanged(item.purchase.purchaseId, it) }
            }
            addTextWatcher(etLr) { text ->
                text.toDoubleOrNull()?.let { onLrChanged(item.purchase.purchaseId, it) }
            }
            addTextWatcher(etNotes) { text ->
                onNotesChanged(item.purchase.purchaseId, text)
            }
        }

        private fun addTextWatcher(view: android.widget.EditText, onChanged: (String) -> Unit) {
            view.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    onChanged(s?.toString() ?: "")
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }
}
