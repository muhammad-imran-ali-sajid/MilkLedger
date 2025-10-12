package com.miassolutions.milkledger.presentation.purchase

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.databinding.ItemPurchaseBinding
import kotlin.math.roundToInt

class PurchaseAdapter(

    private val onSupplierClick: (String) -> Unit,
    private val onVolumeChanged: (String, Double) -> Unit,
    private val onFatChanged: (String, Double) -> Unit,
    private val onLrChanged: (String, Double) -> Unit,
    private val onPaidChanged: (String, Double) -> Unit,
    private val onNotesChanged: (String, String) -> Unit
) : ListAdapter<PurchaseWithSupplier, PurchaseAdapter.PurchaseViewHolder>(DiffCallback) {

    private var recyclerView: RecyclerView? = null

    var isEditable = true
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    companion object DiffCallback : DiffUtil.ItemCallback<PurchaseWithSupplier>() {
        override fun areItemsTheSame(
            oldItem: PurchaseWithSupplier,
            newItem: PurchaseWithSupplier
        ): Boolean {
            return oldItem.purchase.purchaseId == newItem.purchase.purchaseId
        }

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
        val binding: ItemPurchaseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseWithSupplier) = with(binding) {

            val enabled = isEditable

            etVolume.isEnabled = enabled
            etFat.isEnabled = enabled
            etLr.isEnabled = enabled
            etPaid.isEnabled = enabled
            etNotes.isEnabled = enabled


            val bgRes = if (enabled) R.drawable.bg_edit_enabled else R.drawable.bg_edit_disabled

            etVolume.setBackgroundResource(bgRes)
            etFat.setBackgroundResource(bgRes)
            etLr.setBackgroundResource(bgRes)
            etPaid.setBackgroundResource(bgRes)
            etNotes.setBackgroundResource(bgRes)


            binding.root.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (bindingAdapterPosition % 2 == 0) R.color.white else R.color.grey
                )
            )

            tvSupplierName.text = item.supplier.supplierName
            tvPrice.text = "${item.purchase.price.roundToInt()}"
            tvTs.text = "%.3f".format(item.purchase.ts)
            tvBalance.text = "${item.purchase.balance.roundToInt()}"

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

            etVolume.clearTextWatchers()
            etFat.clearTextWatchers()
            etLr.clearTextWatchers()
            etPaid.clearTextWatchers()
            etNotes.clearTextWatchers()

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

            // Select-all behavior on focus and click
            etVolume.enableSelectAll()
            etFat.enableSelectAll()
            etLr.enableSelectAll()
            etPaid.enableSelectAll()
            etNotes.enableSelectAll()

            tvSupplierName.setOnClickListener {
                onSupplierClick(item.purchase.purchaseId)
            }

            etNotes.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                    moveFocusToNextItemVolume(bindingAdapterPosition)
                    true
                } else {
                    false
                }
            }
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    private fun moveFocusToNextItemVolume(currentPosition: Int) {
        val nextPosition = currentPosition + 1
        if (nextPosition < itemCount) {
            recyclerView?.post {
                recyclerView?.smoothScrollToPosition(nextPosition)

                recyclerView?.postDelayed({
                    val holder = recyclerView?.findViewHolderForAdapterPosition(nextPosition)
                            as? PurchaseViewHolder
                    holder?.binding?.etVolume?.requestFocus()
                }, 100)
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

private fun EditText.enableSelectAll() {
    setOnFocusChangeListener { v, hasFocus ->
        if (hasFocus) {
            post { selectAll() }
        }
    }
    setOnClickListener {
        selectAll()
    }
}
