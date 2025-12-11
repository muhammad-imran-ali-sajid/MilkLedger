package com.miassolutions.milkledger.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.repository.AnalyticsRepository
import com.miassolutions.milkledger.presentation.datefilter.DatePeriod
import com.miassolutions.milkledger.presentation.stats.AnalyticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    // -------------------------------------------------------------------------
    // INIT
    // -------------------------------------------------------------------------
    init {
        loadAllRecords()
    }

    // -------------------------------------------------------------------------
    // PUBLIC ROUTER (Just like ProfitViewModel)
    // -------------------------------------------------------------------------
    fun loadData(period: DatePeriod) {
        when (period) {
            is DatePeriod.Daily -> fetchDaily(period.date)
            is DatePeriod.Weekly -> fetchWeekly(period.start, period.end)
            is DatePeriod.Monthly -> fetchMonthly(period.month)
            is DatePeriod.Yearly -> fetchYearly(period.year)
            DatePeriod.All -> loadAllRecords()
            is DatePeriod.Custom -> fetchCustom(period.start, period.end)
        }
    }

    // -------------------------------------------------------------------------
    // ALL RECORDS
    // -------------------------------------------------------------------------
    private fun loadAllRecords() {
        viewModelScope.launch {
            combine(
                repository.getTotalMilkPurchaseAll(),
                repository.getTotalMilkSoldAll(),
                repository.getTotalSalesAll(),
                repository.getTotalPurchasesAll(),
                repository.getTotalFixedExpenses(),
                repository.getGrossProfitAll(),
                repository.getTotalFat(),
                repository.getTotalLr(),
                repository.getTotalTs(),
                repository.getTotalPersonalExpense()
            ) { results ->

                mapAnalyticsResults(
                    milkPurchase = results[0] ?: 0.0,
                    milkSold = results[1] ?: 0.0,
                    totalSales = results[2] ?: 0.0,
                    totalPurchase = results[3] ?: 0.0,
                    totalExpense = results[4] ?: 0.0,
                    profit = results[5] as Double,
                    fat = results[6] ?: 0.0,
                    lr = results[7] ?: 0.0,
                    ts = results[8] ?: 0.0,
                    personalExpense = results[9] ?: 0.0,
                    start = null,
                    end = null,
                    periodLabel = "All Records"
                )
            }.collect { ui ->
                _uiState.update { ui }
            }
        }
    }

    // -------------------------------------------------------------------------
    // DAILY
    // -------------------------------------------------------------------------
    private fun fetchDaily(date: LocalDate) {
        loadRange(date, date)
    }

    // -------------------------------------------------------------------------
    // WEEKLY
    // -------------------------------------------------------------------------
    private fun fetchWeekly(start: LocalDate, end: LocalDate) {
        loadRange(start, end)
    }

    // -------------------------------------------------------------------------
    // MONTHLY
    // -------------------------------------------------------------------------
    private fun fetchMonthly(yearMonth: YearMonth) {
        val start = yearMonth.atDay(1)
        val end = yearMonth.atEndOfMonth()
        loadRange(start, end)
    }

    // -------------------------------------------------------------------------
    // YEARLY
    // -------------------------------------------------------------------------
    private fun fetchYearly(year: Int) {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        loadRange(start, end)
    }

    // -------------------------------------------------------------------------
    // CUSTOM
    // -------------------------------------------------------------------------
    private fun fetchCustom(start: LocalDate?, end: LocalDate?) {
        if (start == null || end == null) {
            loadAllRecords()
            return
        }
        loadRange(start, end)
    }

    // -------------------------------------------------------------------------
    // SHARED RANGE LOADER (Your combine block consolidated)
    // -------------------------------------------------------------------------
    private fun loadRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            combine(
                repository.getTotalMilkPurchaseBetween(start, end),
                repository.getTotalMilkSoldBetween(start, end),
                repository.getTotalSalesBetween(start, end),
                repository.getTotalPurchasesBetween(start, end),
                repository.getTotalFixedExpensesBetween(start, end),
                repository.getProfitBetween(start, end),
                repository.getAvgFatBetween(start, end),
                repository.getAvgLrBetween(start, end),
                repository.getTotalTsBetween(start, end),
                repository.getTotalMilkWithFatAndLr(start, end),
                repository.getTotalPersonalExpensesBetween(start, end)
            ) { results ->

                mapAnalyticsResults(
                    milkPurchase = results[0] ?: 0.0,
                    milkSold = results[1] ?: 0.0,
                    totalSales = results[2] ?: 0.0,
                    totalPurchase = results[3] ?: 0.0,
                    totalExpense = results[4] ?: 0.0,
                    profit = results[5] as Double,
                    fat = results[6] ?: 0.0,
                    lr = results[7] ?: 0.0,
                    ts = results[8] ?: 0.0,
                    totalMilkWithFatAndLr = results[9] ?: 0.0,
                    personalExpense = results[10] ?: 0.0,
                    start = start,
                    end = end,
                    periodLabel = formatPeriodLabel(start, end)
                )
            }.collect { ui ->
                _uiState.update { ui }
            }
        }
    }

    // -------------------------------------------------------------------------
    // CLEAN MAPPING FUNCTION (Removes repeated code)
    // -------------------------------------------------------------------------
    private fun mapAnalyticsResults(
        milkPurchase: Double,
        milkSold: Double,
        totalSales: Double,
        totalPurchase: Double,
        totalExpense: Double,
        profit: Double,
        fat: Double,
        lr: Double,
        ts: Double,
        personalExpense: Double,
        start: LocalDate?,
        end: LocalDate?,
        periodLabel: String,
        totalMilkWithFatAndLr: Double = 0.0,
    ): AnalyticsUiState {

        val avgCP = if (milkPurchase > 0) totalPurchase / milkPurchase else null
        val avgSP = if (milkPurchase > 0) totalSales / milkPurchase else null
        val difference = if (avgCP != null && avgSP != null) avgSP - avgCP else null

        return AnalyticsUiState(
            milkPurchase = milkPurchase,
            milkSold = milkSold,
            avgCP = avgCP,
            avgSP = avgSP,
            difference = difference,
            salesTotal = totalSales,
            purchaseTotal = totalPurchase,
            fixedExpense = totalExpense,
            personalExpense = personalExpense,
            profit = profit,
            profitAfter = profit - personalExpense,
            avgFat = fat,
            avgLr = lr,
            totalTs = ts,
            totalMilkWithFatAndLr = totalMilkWithFatAndLr,
            startDate = start,
            endDate = end,
            period = periodLabel
        )
    }
}
