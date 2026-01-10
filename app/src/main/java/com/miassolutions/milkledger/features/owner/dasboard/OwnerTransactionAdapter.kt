package com.miassolutions.milkledger.features.owner.dasboard


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.databinding.ItemOwnerTransactionBinding
import com.miassolutions.milkledger.features.owner.domain.OwnerTransactionUiModel
import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
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
            tvDate.text = item.dateMillis.toLocalDate().toCompleteDateFormat()
            tvAmount.text = "- ${item.amount.toPrice()}" // Minus sign for visual clarity

            // 2. Title & Icon Logic
            if (item.isPersonalExpense) {
                // 🛍️ Personal Expense
                tvTitle.text = "Personal Expense"
//                ivIcon.setImageResource(R.drawable.ic_receipt_long_24) // Receipt Icon
//                iconBg.setBackgroundResource(R.drawable.bg_circle_light_blue) // Blue bg
                ivIcon.setColorFilter(ContextCompat.getColor(root.context, R.color.blue_700))
            } else {
                // 💵 Cash Withdrawal
                tvTitle.text = "Cash Withdrawal"
//                ivIcon.setImageResource(R.drawable.ic_money_off_24) // Money Icon
//                iconBg.setBackgroundResource(R.drawable.bg_circle_light_red) // Red bg
                ivIcon.setColorFilter(ContextCompat.getColor(root.context, R.color.red))
            }

                tvNote.text = item.note
            // 3. Note
            if (item.note.isNotBlank()) {
                tvNote.isVisible = true
            } else {
                tvNote.isVisible = false
            }

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