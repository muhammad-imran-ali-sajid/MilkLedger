package com.miassolutions.milkledger.presentation.stats

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
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

    // Define constants for view types
    private val TYPE_HEADER = 0
    private val TYPE_CUSTOMER = 1

    private val TYPE_TOTAL_SUMMARY = 4
    private val TYPE_SUPPLIER = 2
    private val TYPE_EXPENSE = 3

    // --- 1. Determine View Type ---
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is StatListItem.Header -> TYPE_HEADER
            is StatListItem.CustomerItem -> TYPE_CUSTOMER
            is StatListItem.SupplierItem -> TYPE_SUPPLIER
            is StatListItem.ExpenseItem -> TYPE_EXPENSE
            is StatListItem.TotalSummary -> TYPE_TOTAL_SUMMARY
        }
    }

    // --- 2. Create the View Holder ---
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                // Inflate header layout (e.g., R.layout.item_header)
                val binding = ItemHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            TYPE_CUSTOMER -> {
                // Inflate customer item layout (e.g., R.layout.item_customer_paid)
                val binding = ItemCustomerPaidBinding.inflate(inflater, parent, false)
                CustomerViewHolder(binding)
            }

            TYPE_SUPPLIER -> {
                // Inflate supplier item layout (e.g., R.layout.item_supplier_paid)
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

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    // --- 3. Bind the Data ---
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is StatListItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is StatListItem.CustomerItem -> (holder as CustomerViewHolder).bind(item.summary)
            is StatListItem.SupplierItem -> (holder as SupplierViewHolder).bind(item.summary)
            is StatListItem.ExpenseItem -> (holder as ExpenseViewHolder).bind(item.summary)
            is StatListItem.TotalSummary -> (holder as TotalSummaryViewHolder).bind(item)
        }
    }
}


// Example ViewHolder implementations
class HeaderViewHolder(private val binding: ItemHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(title: String) {
        binding.titleTextView.text = title
    }
}

class TotalSummaryViewHolder(private val binding: ItemTotalSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: StatListItem.TotalSummary) {
        binding.totalLabelTextView.text = summary.label
        binding.totalAmountTextView.text = summary.amount.toPriceStr() // Use your extension function
    }
}

class CustomerViewHolder(private val binding: ItemCustomerPaidBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: CustomerPaidSummary) {
        // e.g., Set text: Ali 199.0
        binding.customerNameTextView.text = summary.customerName
        binding.paidAmountTextView.setTextColor(Color.GREEN)
        binding.paidAmountTextView.text = summary.paidAmount.toPriceStr()
    }
}

class SupplierViewHolder(private val binding: ItemSupplierPaidBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: SupplierPaidSummary) {
        // e.g., Set text: Ali 199.0
        binding.supplierNameTextView.text = summary.supplierName
        binding.paymentAmountTextView.setTextColor(Color.argb(255,255,152,0))
        binding.paymentAmountTextView.text = summary.paidAmount.toPriceStr()
    }
}

class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: ExpenseSummary) {
        binding.apply {
            tvExpense.text = summary.expenseTitle
            tvExpenseAmount.setTextColor(Color.argb(255,240,240,0))
            tvExpenseAmount.text = summary.expenseAmount.toPriceStr()
        }
    }
}


class StatDiffCallback : DiffUtil.ItemCallback<StatListItem>() {

    // Check if two items represent the same entity (or header)
    override fun areItemsTheSame(oldItem: StatListItem, newItem: StatListItem): Boolean {
        return when {
            // Both are Headers: Check if their title is the same (titles are unique section identifiers)
            oldItem is StatListItem.Header && newItem is StatListItem.Header ->
                oldItem.title == newItem.title

            // Both are Customer Items: Check if their unique saleId is the same
            oldItem is StatListItem.CustomerItem && newItem is StatListItem.CustomerItem ->
                // Assuming saleId is the primary key for sales, which isn't directly in the summary,
                // but the customerName/paidAmount combo is the effective key for this aggregated view.
                // A safer key would be the customerId if available, but let's use the visible data fields:
                oldItem.summary.customerName == newItem.summary.customerName

            // Both are Supplier Items: Check if their supplierName is the same
            oldItem is StatListItem.SupplierItem && newItem is StatListItem.SupplierItem ->
                oldItem.summary.supplierName == newItem.summary.supplierName

            oldItem is StatListItem.ExpenseItem && newItem is StatListItem.ExpenseItem ->
                oldItem.summary.expenseTitle == newItem.summary.expenseTitle

            oldItem is StatListItem.TotalSummary && newItem is StatListItem.TotalSummary ->
                oldItem.label == newItem.label
            // If types are different, they are definitely not the same item
            else -> false
        }
    }

    // Check if the content of two items has changed
    override fun areContentsTheSame(oldItem: StatListItem, newItem: StatListItem): Boolean {
        // Since StatListItem is a sealed class with data classes as subclasses,
        // we can rely on Kotlin's generated 'equals' method for content comparison.
        return oldItem == newItem
    }
}