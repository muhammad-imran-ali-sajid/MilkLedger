package com.miassolutions.milkledger.features.cashflow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.miassolutions.milkledger.utils.extensions.hide
import com.miassolutions.milkledger.utils.extensions.show
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.utils.extensions.toPriceStr
import com.miassolutions.milkledger.databinding.*
import java.time.LocalDate

class StatAdapter : androidx.recyclerview.widget.ListAdapter<StatListItem, RecyclerView.ViewHolder>(StatDiffCallback()) {

    private val TYPE_HEADER = 0
    private val TYPE_CUSTOMER = 1
    private val TYPE_SUPPLIER = 2
    private val TYPE_EXPENSE = 3
    private val TYPE_PERSONAL_EXPENSE = 4
    private val TYPE_PROFIT = 5

    private val TYPE_TOTAL_SUMMARY = 6
    private val TYPE_EMPTY = 7

    private val TYPE_TOTAL_MILK_SUMMARY = 8

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
            is StatListItem.MilkTotalSummary -> TYPE_TOTAL_MILK_SUMMARY
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
            TYPE_TOTAL_MILK_SUMMARY -> MilkTotalSummaryViewHolder(
                ItemTotalMilkSummaryBinding.inflate(inflater, parent, false)
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
                (holder as HeaderViewHolder).bind(item)

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

            is StatListItem.MilkTotalSummary -> (holder as MilkTotalSummaryViewHolder).bind(item)
        }
    }
}

// ----------------- ViewHolders --------------------

class HeaderViewHolder(private val binding: ItemHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: StatListItem.Header) {
        if(item.volume == null)
            binding.tvTitle2.hide()
        else
            binding.tvTitle2.show()

        binding.tvTitle.text = item.title
        binding.tvTitle2.text = item.volume ?: ""
        binding.tvTitle3.text = item.amount
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
        binding.tvLabel.text = item.label
        binding.tvVolume.text = item.amount.toPriceStr()
    }
}

class MilkTotalSummaryViewHolder(private val binding: ItemTotalMilkSummaryBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(item: StatListItem.MilkTotalSummary) {
        binding.tvLabel.text = item.label
        binding.tvPaidAmount.text = item.amount.toPriceStr()
        binding.tvVolume.text = item.volume.toString()
    }
}

class CustomerViewHolder(private val binding: ItemCustomerPaidBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: CustomerPaidSummary) {
        binding.tvCustomerName.text = summary.customerName
        binding.tvPaidAmount.text = summary.paidAmount.toPriceStr()
        binding.tvVolume.text = summary.volume.toString()
    }
}

class SupplierViewHolder(private val binding: ItemSupplierPaidBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(summary: SupplierPaidSummary) {
        binding.tvSupplierName.text = summary.supplierName
        binding.tvPaidAmount.text = summary.paidAmount.toPriceStr()
        binding.tvVolume.text = summary.volume.toString()
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
        val date = LocalDate.parse(summary.date)
        binding.tvDate.text = date.toDisplayFormat()
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

            oldItem is StatListItem.MilkTotalSummary && newItem is StatListItem.MilkTotalSummary ->
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
