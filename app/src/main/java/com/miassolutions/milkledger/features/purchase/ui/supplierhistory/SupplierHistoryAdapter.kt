package com.miassolutions.milkledger.features.purchase.ui.supplierhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemSupplierHistoryBinding
import com.miassolutions.milkledger.features.purchase.model.MilkPurchaseUiModel
import com.miassolutions.milkledger.utils.extensions.highlightCurrentText
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.setHighlightedText
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice
import java.util.Locale

class SupplierHistoryAdapter :
    ListAdapter<MilkPurchaseUiModel, SupplierHistoryAdapter.ViewHolder>(DiffCallback) {

    private var searchQuery: String = ""

    fun submitListWithSearch(
        list: List<MilkPurchaseUiModel>,
        query: String
    ) {
        val newQuery = query.trim()
        val queryChanged = searchQuery != newQuery

        searchQuery = newQuery
        submitList(list)

        if (queryChanged) {
            notifyItemRangeChanged(0, itemCount)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemSupplierHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), searchQuery)
    }

    class ViewHolder(
        private val binding: ItemSupplierHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MilkPurchaseUiModel,
            query: String
        ) = with(binding) {

            val context = root.context

            val dateText = item.dateMillis.toLocalDate().toCompleteDateFormat()

            tvDate.setHighlightedText(
                value = dateText,
                query = query,
            )

            rateAlert.isVisible = false
            tvPaymentDate.isVisible = false

            tvMilk.setHighlightedText(
                value = String.format(Locale.getDefault(), "%.1f", item.volume),
                query = query,
            )

            tvFat.setHighlightedText(
                value = String.format(Locale.getDefault(), "%.1f", item.fat),
                query = query,
            )

            tvLr.setHighlightedText(
                value = String.format(Locale.getDefault(), "%.1f", item.lr),
                query = query,
            )

            tvTs.setHighlightedText(
                value = String.format(Locale.getDefault(), "%.2f", item.ts),
                query = query,
            )

            tvPrice.setHighlightedText(
                value = item.totalAmount.toPrice(),
                query = query,
            )

            val isRateChanged =
                item.previousRate != null && item.previousRate != item.rate

            if (isRateChanged) {
                val rateText = "Rate Alert: ${item.previousRate} -> ${item.rate}"

                rateAlert.show()
                rateAlert.setHighlightedText(
                    value = rateText,
                    query = query,
                )

                root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.md_theme_primaryContainer
                    )
                )
            } else {
                root.setCardBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.md_theme_surfaceVariant
                    )
                )
            }

            if (item.paymentMade > 0) {
                tvPayment.isVisible = true
                tvPayment.setHighlightedText(
                    value = item.paymentMade.toPrice(),
                    query = query,
                )

                val payDate = item.paymentDate
                val purchaseDate = item.dateMillis.toLocalDate()

                if (payDate != null && !payDate.isEqual(purchaseDate)) {
                    tvPaymentDate.isVisible = true
                    tvPaymentDate.setHighlightedText(
                        value = "(${payDate.dayOfMonth}/${payDate.monthValue})",
                        query = query,
                    )
                } else {
                    tvPaymentDate.isVisible = false
                }
            } else {
                tvPayment.isVisible = true
                tvPayment.setHighlightedText(
                    value = "-",
                    query = query,
                )
                tvPaymentDate.isVisible = false
            }

            tvBalance.setBalanceWithColor(item.currentBalance)
            tvBalance.highlightCurrentText(
                query = query,
            )

            if (!item.note.isNullOrBlank()) {
                val noteText =
                    if (item.paymentDate != null) {
                        "Note: ${item.note} (${item.paymentDate!!.toDisplayDate()})"
                    } else {
                        "Note: ${item.note}"
                    }

                tvNotes.isVisible = true
                tvNotes.setHighlightedText(
                    value = noteText,
                    query = query,
                )
            } else {
                tvNotes.isVisible = false
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MilkPurchaseUiModel>() {

        override fun areItemsTheSame(
            oldItem: MilkPurchaseUiModel,
            newItem: MilkPurchaseUiModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MilkPurchaseUiModel,
            newItem: MilkPurchaseUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
