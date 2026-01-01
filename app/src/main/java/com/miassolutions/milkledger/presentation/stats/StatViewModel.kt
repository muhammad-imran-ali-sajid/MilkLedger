package com.miassolutions.milkledger.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.utils.util.DateRangeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatViewModel @Inject constructor(
    private val repository: StatsRepository
) : ViewModel() {

    private val _balanceFlow = MutableStateFlow(0.0)
    val balanceFlow: StateFlow<Double> = _balanceFlow


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

            val records: List<StateRecord> = repository.getTotalsForRange(start, end)

            val customers = records.filter { it.category == StateRecord.Category.CUSTOMER }
            val suppliers = records.filter { it.category == StateRecord.Category.SUPPLIER }
            val businessExpense = records.filter { it.category == StateRecord.Category.EXPENSE }
            val personalExpense = records.filter { it.category == StateRecord.Category.OTHER }
            val profits = records.filter { it.category == StateRecord.Category.PROFIT }


            val customerTotal = customers.sumOf { it.amount }
            val customerVolume: Double = customers.sumOf { it.volume ?: 0.0 }
            val supplierVolume: Double = suppliers.sumOf { it.volume ?: 0.0 }
            val supplierTotal = suppliers.sumOf { it.amount }
            val businessExpenseTotal = businessExpense.sumOf { it.amount }
            val personalExpenseTotal = personalExpense.sumOf { it.amount }
            val profitTotal = profits.sumOf { it.amount }

            // 🔥 Calculate balance dynamically
            _balanceFlow.value =
                customerTotal - supplierTotal - businessExpenseTotal - personalExpenseTotal - profitTotal

            val finalList = buildList {
                add(StatListItem.Header("Customers", "Volume", "Amount"))
                if (customers.isEmpty()) add(StatListItem.Empty("No customer payments"))
                else {
                    customers.forEach { rec ->
                        add(
                            StatListItem.CustomerItem(
                                CustomerPaidSummary(rec.name, rec.amount, rec.volume)
                            )
                        )
                    }
                    add(StatListItem.MilkTotalSummary("TOTAL", customerVolume, customerTotal))
                }

                add(StatListItem.Header("Suppliers", "Volume", "Amount"))
                if (suppliers.isEmpty()) add(StatListItem.Empty("No supplier payments"))
                else {
                    suppliers.forEach { rec ->
                        add(
                            StatListItem.SupplierItem(
                                SupplierPaidSummary(rec.name, rec.amount, rec.volume)
                            )
                        )
                    }
                    add(StatListItem.MilkTotalSummary("TOTAL", supplierVolume, supplierTotal))
                }

                add(
                    StatListItem.Header(
                        title = "Business Exp",
                        amount = "Amount"
                    )
                )
                if (businessExpense.isEmpty()) add(StatListItem.Empty("No expenses"))
                else {
                    businessExpense.forEach { rec ->
                        add(
                            StatListItem.BusinessExpenseItem(
                                BusinessExpenseSummary(rec.name, rec.amount)
                            )
                        )
                    }
                    add(StatListItem.TotalSummary("TOTAL", businessExpenseTotal))
                }

                add(
                    StatListItem.Header(
                        title = "Personal Exp",
                        amount = "Amount"
                    )
                )
                if (personalExpense.isEmpty()) add(StatListItem.Empty("No personal expenses"))
                else {
                    personalExpense.forEach { record ->
                        add(
                            StatListItem.PersonalExpenseItem(
                                PersonalExpenseSummary(record.name, record.amount)
                            )
                        )
                    }
                    add(StatListItem.TotalSummary("TOTAL", personalExpenseTotal))
                }

                add(
                    StatListItem.Header(
                        title = "Profit",
                        amount = "Amount"
                    )
                )
                if (profits.isEmpty()) add(StatListItem.Empty("No profit yet"))
                else {
                    profits.forEach { r ->
                        add(
                            StatListItem.ProfitItem(
                                ProfitSummary(r.name, r.amount)
                            )
                        )
                    }
                }

                add(StatListItem.TotalSummary("TOTAL", profitTotal))
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
