package com.miassolutions.milkledger.presentation.stats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.databinding.*

class StatAdapter : ListAdapter<StatListItem, RecyclerView.ViewHolder>(StatDiffCallback()) {

    private val TYPE_HEADER = 0
    private val TYPE_CUSTOMER = 1
    private val TYPE_SUPPLIER = 2
    private val TYPE_EXPENSE = 3
    private val TYPE_PERSONAL_EXPENSE = 4
    private val TYPE_PROFIT = 5

    private val TYPE_TOTAL_SUMMARY = 6
    private val TYPE_EMPTY = 7

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is StatListItem.Header -> TYPE_HEADER
            is StatListItem.CustomerItem -> TYPE_CUSTOMER
            is StatListItem.SupplierItem -> TYPE_SUPPLIER
            is StatListItem.BusinessExpenseItem -> TYPE_EXPENSE
            is StatListItem.PersonalExpenseItem -> TYPE_PERSONAL_EXPENSE
            is StatListItem.ProfitItem -> TYPE_PROFIT
            is StatListItem.TotalSummary -> TYPE_TOTAL_SUMMARY
            is StatListItem.Empty -> TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_HEADER -> HeaderViewHolder(
                ItemHeaderBinding.inflate(inflater, parent, false)
            )

            TYPE_CUSTOMER -> CustomerViewHolder(
                ItemCustomerPaidBinding.inflate(inflater, parent, false)
            )

            TYPE_SUPPLIER -> SupplierViewHolder(
                ItemSupplierPaidBinding.inflate(inflater, parent, false)
            )

            TYPE_EXPENSE -> ExpenseViewHolder(
                ItemExpenseBinding.inflate(inflater, parent, false)
            )
            TYPE_PERSONAL_EXPENSE -> PersonalExpenseViewHolder(
                ItemOtherExpenseBinding.inflate(inflater, parent, false)
            )

            TYPE_PROFIT -> ProfitViewHolder(
                ItemProfitOverviewBinding.inflate(inflater, parent, false)
            )


            TYPE_TOTAL_SUMMARY -> TotalSummaryViewHolder(
                ItemTotalSummaryBinding.inflate(inflater, parent, false)
            )

            TYPE_EMPTY -> EmptyViewHolder(
                ItemEmptyBinding.inflate(inflater, parent, false) //
            )



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

            is StatListItem.BusinessExpenseItem ->
                (holder as ExpenseViewHolder).bind(item.summary)

            is StatListItem.PersonalExpenseItem ->
                (holder as PersonalExpenseViewHolder).bind(item.summary)

            is StatListItem.ProfitItem ->
                (holder as ProfitViewHolder).bind(item.summary)

            is StatListItem.TotalSummary ->
                (holder as TotalSummaryViewHolder).bind(item)

            is StatListItem.Empty ->
                (holder as EmptyViewHolder).bind(item.message)
        }
    }
}

// ----------------- ViewHolders --------------------

class HeaderViewHolder(private val binding: ItemHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(title: String) {
        binding.titleTextView.text = title
    }
}

class EmptyViewHolder(private val binding: ItemEmptyBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(message: String) {
        binding.emptyMessage.text = message
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
        binding.paidAmountTextView.text = "${summary.paidAmount.toPriceStr()}(${summary.volume})"
    }
}

class SupplierViewHolder(private val binding: ItemSupplierPaidBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: SupplierPaidSummary) {
        binding.supplierNameTextView.text = summary.supplierName
        binding.paymentAmountTextView.text = "${summary.paidAmount.toPriceStr()}(${summary.volume})"
    }
}

class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: BusinessExpenseSummary) {
        binding.tvExpense.text = summary.expenseTitle
        binding.tvExpenseAmount.text = summary.expenseAmount.toPriceStr()
    }
}


class PersonalExpenseViewHolder(private val binding: ItemOtherExpenseBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: PersonalExpenseSummary) {
        binding.tvExpense.text = summary.expenseTitle
        binding.tvExpenseAmount.text = summary.expenseAmount.toPriceStr()
    }
}

class ProfitViewHolder(private val binding: ItemProfitOverviewBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: ProfitSummary) {
        binding.tvDate.text = summary.date
        binding.tvProfitReceived.text = summary.profitAmount.toPriceStr()
    }
}


// ----------------- DiffUtil --------------------

class StatDiffCallback : DiffUtil.ItemCallback<StatListItem>() {

    override fun areItemsTheSame(oldItem: StatListItem, newItem: StatListItem): Boolean {
        return when {

            oldItem is StatListItem.Header && newItem is StatListItem.Header ->
                oldItem.title == newItem.title

            oldItem is StatListItem.CustomerItem && newItem is StatListItem.CustomerItem ->
                oldItem.summary.customerName == newItem.summary.customerName

            oldItem is StatListItem.SupplierItem && newItem is StatListItem.SupplierItem ->
                oldItem.summary.supplierName == newItem.summary.supplierName

            oldItem is StatListItem.BusinessExpenseItem && newItem is StatListItem.BusinessExpenseItem ->
                oldItem.summary.expenseTitle == newItem.summary.expenseTitle

            oldItem is StatListItem.TotalSummary && newItem is StatListItem.TotalSummary ->
                oldItem.label == newItem.label

            oldItem is StatListItem.Empty && newItem is StatListItem.Empty ->
                oldItem.message == newItem.message

            oldItem is StatListItem.PersonalExpenseItem && newItem is StatListItem.PersonalExpenseItem ->
                oldItem.summary.expenseTitle == newItem.summary.expenseTitle

            oldItem is StatListItem.ProfitItem && newItem is StatListItem.ProfitItem ->
                oldItem.summary.date  == newItem.summary.date

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: StatListItem, newItem: StatListItem): Boolean {
        return oldItem == newItem
    }
}
