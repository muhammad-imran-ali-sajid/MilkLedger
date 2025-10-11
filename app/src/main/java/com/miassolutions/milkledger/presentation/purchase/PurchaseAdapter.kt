package com.miassolutions.milkledger.presentation.purchase

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
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
    private val onPaidChanged: (String, Double) -> Unit,
    private val onNotesChanged: (String, String) -> Unit
) : ListAdapter<PurchaseWithSupplier, PurchaseAdapter.PurchaseViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<PurchaseWithSupplier>() {
        override fun areItemsTheSame(
            oldItem: PurchaseWithSupplier,
            newItem: PurchaseWithSupplier
        ) = oldItem.purchase.purchaseId == newItem.purchase.purchaseId

        override fun areContentsTheSame(
            oldItem: PurchaseWithSupplier,
            newItem: PurchaseWithSupplier
        ) = oldItem == newItem
    }

    init {
        setHasStableIds(true) // preserve focus stability
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).purchase.purchaseId.hashCode().toLong()
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

        private var currentItemId: String? = null

        fun bind(item: PurchaseWithSupplier) = with(binding) {
            currentItemId = item.purchase.purchaseId

            tvSupplierName.text = item.supplier.supplierName
            tvPrice.text = "Rs. %.2f".format(item.purchase.price)
            tvTs.text = "%.2f".format(item.purchase.ts)
            tvBalance.text = "%.2f".format(item.purchase.paid)

            // --- safely set text without disturbing cursor ---

            if (!etVolume.hasFocus()) {
                etVolume.safeSetText(trimTrailingZeros(item.purchase.volume))
            }
            if (!etFat.hasFocus()) {
                etFat.safeSetText(trimTrailingZeros(item.purchase.fat))
            }
            if (!etLr.hasFocus()) {
                etLr.safeSetText(trimTrailingZeros(item.purchase.lr))
            }

            if (!etPaid.hasFocus()) {
                etPaid.safeSetText(trimTrailingZeros(item.purchase.paid))
            }
            etNotes.safeSetText(item.purchase.notes.orEmpty())

            // --- clear old watchers before adding new ones ---
            etVolume.clearTextWatchers()
            etFat.clearTextWatchers()
            etLr.clearTextWatchers()
            etPaid.clearTextWatchers()
            etNotes.clearTextWatchers()

            // --- add fresh text watchers ---
            etVolume.addTextWatcher(simpleWatcher { s ->
                if (etVolume.hasFocus()) {
                    s.toDoubleOrNull()?.let { onVolumeChanged(item.purchase.purchaseId, it) }
                }
            })

            etFat.addTextWatcher(simpleWatcher { s ->
                if (etFat.hasFocus()) {
                    s.toDoubleOrNull()?.let { onFatChanged(item.purchase.purchaseId, it) }
                }
            })

            etLr.addTextWatcher(simpleWatcher { s ->
                if (etLr.hasFocus()) {
                    s.toDoubleOrNull()?.let { onLrChanged(item.purchase.purchaseId, it) }
                }
            })

            etPaid.addTextWatcher(simpleWatcher { s ->
                if (etPaid.hasFocus()) {
                    s.toDoubleOrNull()?.let { onPaidChanged(item.purchase.purchaseId, it) }
                }
            })

            etNotes.addTextWatcher(simpleWatcher { s ->
                if (etNotes.hasFocus()) {
                    onNotesChanged(item.purchase.purchaseId, s)
                }
            })

            // --- supplier click ---
            tvSupplierName.setOnClickListener {
                onSupplierClick(item.purchase.purchaseId)
            }
        }
    }
}

// -------------------- Extensions --------------------

private fun trimTrailingZeros(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}


private fun simpleWatcher(onAfter: (String) -> Unit): TextWatcher {
    return object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            onAfter(s?.toString().orEmpty())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }
}

private fun EditText.clearTextWatchers() {
    val watchers = this.tag as? MutableList<TextWatcher> ?: mutableListOf()
    watchers.forEach { removeTextChangedListener(it) }
    watchers.clear()
    this.tag = watchers
}

private fun EditText.addTextWatcher(watcher: TextWatcher) {
    val watchers = (this.tag as? MutableList<TextWatcher>) ?: mutableListOf()
    watchers.add(watcher)
    this.tag = watchers
    addTextChangedListener(watcher)
}

private fun EditText.safeSetText(newText: String) {
    if (text.toString() != newText) {
        val hadFocus = hasFocus()
        val cursorPos = selectionStart
        setText(newText)
        if (hadFocus) {
            val pos = cursorPos.coerceIn(0, newText.length)
            setSelection(pos)
        }
    }
}
