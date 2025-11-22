package com.miassolutions.milkledger.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.DateRangeUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatViewModel @Inject constructor(
    private val repository: StatsRepository
) : ViewModel() {

    // date stream (keeps current selected date; still useful for single-day UI)
    private val _targetDateFlow = MutableStateFlow(LocalDate.now())
    val targetDate: StateFlow<LocalDate> = _targetDateFlow.asStateFlow()

    // range list exposed to UI (RecyclerView)
    private val _rangeList = MutableStateFlow<List<StatListItem>>(emptyList())
    val rangeList: StateFlow<List<StatListItem>> = _rangeList.asStateFlow()

    private var currentPeriod: Period = Period.DAILY
    private var currentRange: Pair<LocalDate, LocalDate> = LocalDate.now() to LocalDate.now()

    enum class Period { DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM }

    // --- Loaders ---
    fun loadDaily() {
        currentPeriod = Period.DAILY
        val today = LocalDate.now()
        currentRange = today to today
        loadRange(today, today)
    }

    fun loadWeekly() {
        currentPeriod = Period.WEEKLY
        currentRange = DateRangeUtil.thisWeek()
        loadRange(currentRange.first, currentRange.second)
    }

    fun loadMonthly() {
        currentPeriod = Period.MONTHLY
        currentRange = DateRangeUtil.thisMonth()
        loadRange(currentRange.first, currentRange.second)
    }

    fun loadYearly() {
        currentPeriod = Period.YEARLY
        currentRange = DateRangeUtil.thisYear()
        loadRange(currentRange.first, currentRange.second)
    }

    fun loadCustom(start: LocalDate?, end: LocalDate?) {
        currentPeriod = Period.CUSTOM

        if (start == null || end == null) {
            loadAllRecords()
        } else {
            currentRange = start to end
            loadRange(start, end)
        }
    }

    private fun loadAllRecords() {
        // safe epoch as "all time" start
        val epochStart = LocalDate.of(1970, 1, 1)
        val today = LocalDate.now()
        currentRange = epochStart to today
        loadRange(epochStart, today)
    }

    /**
     * Core loader: request aggregated records from repository, convert to StatListItem
     * and build a list with headers and totals for each section.
     */
    fun loadRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            // fetch everything in a single call
            val records: List<StateRecord> = repository.getTotalsForRange(start, end)

            // group records by category for building sections and totals
            val customers = records.filter { it.category == StateRecord.Category.CUSTOMER }
            val suppliers = records.filter { it.category == StateRecord.Category.SUPPLIER }
            val expenses = records.filter { it.category == StateRecord.Category.EXPENSE }

            val customerTotal = customers.sumOf { it.amount }
            val supplierTotal = suppliers.sumOf { it.amount }
            val expenseTotal = expenses.sumOf { it.amount }

            val finalList = buildList {
                // Customers section
                add(StatListItem.Header("Customer Payments"))
                if (customers.isEmpty()) {
                    add(StatListItem.Empty("No customer payments"))
                } else {
                    customers.forEach { rec ->
                        // convert to your domain summary type if you prefer; using simple constructor
                        add(
                            StatListItem.CustomerItem(
                                CustomerPaidSummary(
                                    customerName = rec.name,
                                    paidAmount = rec.amount
                                )
                            )
                        )
                    }
                    add(StatListItem.TotalSummary("TOTAL RECEIVED", customerTotal))
                }

                // Suppliers section
                add(StatListItem.Header("Supplier Payments"))
                if (suppliers.isEmpty()) {
                    add(StatListItem.Empty("No supplier payments"))
                } else {
                    suppliers.forEach { rec ->
                        add(
                            StatListItem.SupplierItem(
                                SupplierPaidSummary(
                                    supplierName = rec.name,
                                    paidAmount = rec.amount
                                )
                            )
                        )
                    }
                    add(StatListItem.TotalSummary("TOTAL PAID", supplierTotal))
                }

                // Expenses section
                add(StatListItem.Header("Expenses"))
                if (expenses.isEmpty()) {
                    add(StatListItem.Empty("No expenses"))
                } else {
                    expenses.forEach { rec ->
                        add(
                            StatListItem.ExpenseItem(
                                ExpenseSummary(
                                    expenseTitle = rec.name,
                                    expenseAmount = rec.amount
                                )
                            )
                        )
                    }
                    add(StatListItem.TotalSummary("TOTAL EXPENSES", expenseTotal))
                }
            }

            // store current range and emit list
            currentRange = start to end
            _rangeList.value = finalList
        }
    }

    private fun shiftRange(direction: Int) {
        val (start, end) = currentRange
        val newRange = when (currentPeriod) {
            Period.DAILY -> start.plusDays(direction.toLong()) to end.plusDays(direction.toLong())
            Period.WEEKLY -> start.plusWeeks(direction.toLong()) to end.plusWeeks(direction.toLong())
            Period.MONTHLY -> start.plusMonths(direction.toLong()) to end.plusMonths(direction.toLong())
            Period.YEARLY -> start.plusYears(direction.toLong()) to end.plusYears(direction.toLong())
            Period.CUSTOM -> return // Skip shifting custom range
        }
        currentRange = newRange
        loadRange(newRange.first, newRange.second)
    }

    // keep target date helpers (if UI uses them)
    fun setTargetDate(newDate: LocalDate) {
        if (newDate != _targetDateFlow.value) {
            _targetDateFlow.value = newDate
        }
    }

    fun onNextClicked() {
        _targetDateFlow.value = _targetDateFlow.value.plusDays(1)
    }

    fun onPrevClicked() {
        _targetDateFlow.value = _targetDateFlow.value.minusDays(1)
    }
}
