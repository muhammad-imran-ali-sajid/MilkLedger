package com.miassolutions.milkledger.features.sale.customerhistory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemCustomerDetailBinding
import com.miassolutions.milkledger.features.sale.model.MilkSaleUiModel
import com.miassolutions.milkledger.utils.extensions.highlightCurrentText
import com.miassolutions.milkledger.utils.extensions.setBalanceWithColor
import com.miassolutions.milkledger.utils.extensions.setHighlightedText
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toFormattedMilk
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class CustomerHistoryAdapter :
    ListAdapter<MilkSaleUiModel, CustomerHistoryAdapter.HistoryViewHolder>(DiffCallback) {

    private var searchQuery: String = ""

    fun submitListWithSearch(
        list: List<MilkSaleUiModel>,
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
    ): HistoryViewHolder {
        val binding = ItemCustomerDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: HistoryViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position), searchQuery)
    }

    inner class HistoryViewHolder(
        private val binding: ItemCustomerDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MilkSaleUiModel,
            query: String
        ) = with(binding) {

            val context = root.context

            tvDate.setHighlightedText(
                value = item.dateMillis.toLocalDate().toCompleteDateFormat(),
                query = query,
            )

            tvMilk.setHighlightedText(
                value = item.quantity.toFormattedMilk(),
                query = query,
            )

            tvDeduction.setHighlightedText(
                value = item.deduction.toString(),
                query = query,
            )

            tvNetMilk.setHighlightedText(
                value = item.netQuantity.toFormattedMilk(),
                query = query,
            )

            tvPrice.setHighlightedText(
                value = item.totalAmount.toPrice(),
                query = query,
            )

            rateAlert.isVisible = false

            val isRateChanged =
                item.previousRate != null && item.previousRate != item.rate

            if (isRateChanged) {
                val rateAlertText =
                    "Rate Alert: ${item.previousRate} -> ${item.rate}"

                rateAlert.show()
                rateAlert.setHighlightedText(
                    value = rateAlertText,
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

            tvPaymentDate.isVisible = false

            if (item.paymentReceived > 0) {
                tvPayment.setHighlightedText(
                    value = item.paymentReceived.toPrice(),
                    query = query,
                )

                val payDate = item.paymentDateMillis?.toLocalDate()
                val saleDate = item.dateMillis.toLocalDate()

                if (payDate != null && payDate != saleDate) {
                    tvPaymentDate.show()
                    tvPaymentDate.setHighlightedText(
                        value = "(${payDate.toDisplayDate()})",
                        query = query,
                    )
                }
            } else {
                tvPayment.setHighlightedText(
                    value = "-",
                    query = query,
                )
            }

            tvBalance.setBalanceWithColor(item.currentBalance)
            tvBalance.highlightCurrentText(
                query = query,
            )

            if (!item.note.isNullOrBlank()) {
                tvNotes.isVisible = true
                tvNotes.setHighlightedText(
                    value = "Note: ${item.note}",
                    query = query,
                )
            } else {
                tvNotes.isVisible = false
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MilkSaleUiModel>() {

        override fun areItemsTheSame(
            oldItem: MilkSaleUiModel,
            newItem: MilkSaleUiModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MilkSaleUiModel,
            newItem: MilkSaleUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}

