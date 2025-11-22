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

    private val _targetDateFlow = MutableStateFlow(LocalDate.now())
    val targetDate: StateFlow<LocalDate> = _targetDateFlow.asStateFlow()

    private val _rangeList = MutableStateFlow<List<StatListItem>>(emptyList())
    val rangeList: StateFlow<List<StatListItem>> = _rangeList.asStateFlow()

    var currentPeriod: Period = Period.DAILY
    var currentRange: Pair<LocalDate, LocalDate> =
        LocalDate.now() to LocalDate.now()

    enum class Period { DAILY, WEEKLY, MONTHLY, YEARLY, CUSTOM }

    // ------------------------------------------------------------------------
    // Loaders
    // ------------------------------------------------------------------------

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
        val epochStart = LocalDate.of(1970, 1, 1)
        val today = LocalDate.now()
        currentRange = epochStart to today
        loadRange(epochStart, today)
    }

    // ------------------------------------------------------------------------
    // Core loader
    // ------------------------------------------------------------------------

    fun loadRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            val records: List<StateRecord> =
                repository.getTotalsForRange(start, end)

            val customers = records.filter { it.category == StateRecord.Category.CUSTOMER }
            val suppliers = records.filter { it.category == StateRecord.Category.SUPPLIER }
            val expenses = records.filter { it.category == StateRecord.Category.EXPENSE }

            val customerTotal = customers.sumOf { it.amount }
            val supplierTotal = suppliers.sumOf { it.amount }
            val expenseTotal = expenses.sumOf { it.amount }

            val finalList = buildList {

                // Customers Section -----------------------
                add(StatListItem.Header("Customer Payments"))
                if (customers.isEmpty()) {
                    add(StatListItem.Empty("No customer payments"))
                } else {
                    customers.forEach { rec ->
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

                // Suppliers Section -----------------------
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

                // Expenses Section ------------------------
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

            currentRange = start to end
            _rangeList.value = finalList
        }
    }

    // ------------------------------------------------------------------------
    // NEXT / PREV navigation handling
    // ------------------------------------------------------------------------

    fun onNextClicked() {
        when (currentPeriod) {

            Period.DAILY -> {
                val newDate = _targetDateFlow.value.plusDays(1)
                _targetDateFlow.value = newDate
                loadRange(newDate, newDate)
            }

            Period.WEEKLY -> shiftRange(+1)
            Period.MONTHLY -> shiftRange(+1)
            Period.YEARLY -> shiftRange(+1)

            Period.CUSTOM -> {
                // Do nothing
            }
        }
    }

    fun onPrevClicked() {
        when (currentPeriod) {

            Period.DAILY -> {
                val newDate = _targetDateFlow.value.minusDays(1)
                _targetDateFlow.value = newDate
                loadRange(newDate, newDate)
            }

            Period.WEEKLY -> shiftRange(-1)
            Period.MONTHLY -> shiftRange(-1)
            Period.YEARLY -> shiftRange(-1)

            Period.CUSTOM -> {
                // Do nothing
            }
        }
    }

    private fun shiftRange(direction: Int) {
        val (start, end) = currentRange

        val newRange = when (currentPeriod) {
            Period.WEEKLY -> start.plusWeeks(direction.toLong()) to end.plusWeeks(direction.toLong())
            Period.MONTHLY -> start.plusMonths(direction.toLong()) to end.plusMonths(direction.toLong())
            Period.YEARLY -> start.plusYears(direction.toLong()) to end.plusYears(direction.toLong())

            Period.DAILY, Period.CUSTOM -> return
        }

        currentRange = newRange
        loadRange(newRange.first, newRange.second)
    }
}
