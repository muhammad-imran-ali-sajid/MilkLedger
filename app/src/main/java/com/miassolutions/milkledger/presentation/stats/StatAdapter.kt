package com.miassolutions.milkledger.presentation.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.ItemCustomerPaidBinding
import com.miassolutions.milkledger.databinding.ItemExpenseBinding
import com.miassolutions.milkledger.databinding.ItemHeaderBinding
import com.miassolutions.milkledger.databinding.ItemSupplierPaidBinding
import com.miassolutions.milkledger.databinding.ItemTotalSummaryBinding

class StatAdapter : ListAdapter<StatListItem, RecyclerView.ViewHolder>(StatDiffCallback()) {

    private val TYPE_HEADER = 0
    private val TYPE_CUSTOMER = 1
    private val TYPE_SUPPLIER = 2
    private val TYPE_EXPENSE = 3
    private val TYPE_TOTAL_SUMMARY = 4

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is StatListItem.Header -> TYPE_HEADER
            is StatListItem.CustomerItem -> TYPE_CUSTOMER
            is StatListItem.SupplierItem -> TYPE_SUPPLIER
            is StatListItem.ExpenseItem -> TYPE_EXPENSE
            is StatListItem.TotalSummary -> TYPE_TOTAL_SUMMARY
            is StatListItem.Empty -> TODO()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            TYPE_CUSTOMER -> {
                val binding = ItemCustomerPaidBinding.inflate(inflater, parent, false)
                CustomerViewHolder(binding)
            }

            TYPE_SUPPLIER -> {
                val binding = ItemSupplierPaidBinding.inflate(inflater, parent, false)
                SupplierViewHolder(binding)
            }

            TYPE_EXPENSE -> {
                val binding = ItemExpenseBinding.inflate(inflater, parent, false)
                ExpenseViewHolder(binding)
            }

            TYPE_TOTAL_SUMMARY -> {
                val binding = ItemTotalSummaryBinding.inflate(inflater, parent, false)
                TotalSummaryViewHolder(binding)
            }

            else -> error("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {

            is StatListItem.Header ->
                (holder as HeaderViewHolder).bind(item.title)

            is StatListItem.CustomerItem ->
                (holder as CustomerViewHolder).bind(item.summary)

            is StatListItem.SupplierItem ->
                (holder as SupplierViewHolder).bind(item.summary)

            is StatListItem.ExpenseItem ->
                (holder as ExpenseViewHolder).bind(item.summary)

            is StatListItem.TotalSummary ->
                (holder as TotalSummaryViewHolder).bind(item)

            is StatListItem.Empty -> TODO()
        }
    }
}

class HeaderViewHolder(private val binding: ItemHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(title: String) {
        binding.titleTextView.text = title
    }
}

class TotalSummaryViewHolder(private val binding: ItemTotalSummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: StatListItem.TotalSummary) {
        binding.totalLabelTextView.text = item.label
        binding.totalAmountTextView.text = item.amount.toPriceStr()
    }
}

class CustomerViewHolder(private val binding: ItemCustomerPaidBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(summary: CustomerPaidSummary) {
        binding.customerNameTextView.text = summary.customerName
        binding.paidAmountTextView.text = summary.paidAmount.toPriceStr()
    }
}


class SupplierViewHolder(private val binding: ItemSupplierPaidBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(summary: SupplierPaidSummary) {
        binding.supplierNameTextView.text = summary.supplierName
        binding.paymentAmountTextView.text = summary.paidAmount.toPriceStr()
    }
}


class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(summary: ExpenseSummary) {
        binding.tvExpense.text = summary.expenseTitle
        binding.tvExpenseAmount.text = summary.expenseAmount.toPriceStr()
    }
}

class StatDiffCallback : DiffUtil.ItemCallback<StatListItem>() {

    override fun areItemsTheSame(oldItem: StatListItem, newItem: StatListItem): Boolean {
        return when {

            oldItem is StatListItem.Header && newItem is StatListItem.Header ->
                oldItem.title == newItem.title

            oldItem is StatListItem.CustomerItem && newItem is StatListItem.CustomerItem ->
                oldItem.summary.customerName == newItem.summary.customerName

            oldItem is StatListItem.SupplierItem && newItem is StatListItem.SupplierItem ->
                oldItem.summary.supplierName == newItem.summary.supplierName

            oldItem is StatListItem.ExpenseItem && newItem is StatListItem.ExpenseItem ->
                oldItem.summary.expenseTitle == newItem.summary.expenseTitle

            oldItem is StatListItem.TotalSummary && newItem is StatListItem.TotalSummary ->
                oldItem.label == newItem.label

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: StatListItem, newItem: StatListItem): Boolean {
        return oldItem == newItem
    }
}






