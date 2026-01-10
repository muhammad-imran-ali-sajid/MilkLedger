package com.miassolutions.milkledger.features.owner.dasboard


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.databinding.ItemOwnerTransactionBinding
import com.miassolutions.milkledger.features.owner.domain.OwnerTransactionUiModel
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toPrice

class OwnerTransactionAdapter(
    private val onItemClick: (OwnerTransactionUiModel) -> Unit
) :
    ListAdapter<OwnerTransactionUiModel, OwnerTransactionAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemOwnerTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemOwnerTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OwnerTransactionUiModel) = with(binding) {
            // 1. Date & Amount
            tvDate.text = "${item.dateMillis.toLocalDate().toDisplayDate()}: "
            tvAmount.text = "- ${item.amount.toPrice()}" // Minus sign for visual clarity

            // 2. Title & Icon Logic
            if (item.isPersonalExpense) {
                // 🛍️ Personal Expense
                tvTitle.text = "Prsnl Exp."
            } else {
                // 💵 Cash Withdrawal
                tvTitle.text = "Withdrawal"
            }

            tvNote.text = item.note


            root.setOnLongClickListener {
                onItemClick(item)
                true
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<OwnerTransactionUiModel>() {
        override fun areItemsTheSame(
            oldItem: OwnerTransactionUiModel,
            newItem: OwnerTransactionUiModel
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: OwnerTransactionUiModel,
            newItem: OwnerTransactionUiModel
        ) = oldItem == newItem
    }
}