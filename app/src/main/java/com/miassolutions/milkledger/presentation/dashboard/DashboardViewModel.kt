package com.miassolutions.milkledger.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.DateRangeUtil
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.mapper.toRoomEntity
import com.miassolutions.milkledger.data.repository.AnalyticsRepository
import com.miassolutions.milkledger.data.repository.AppData
import com.miassolutions.milkledger.data.repository.DataRepository
import com.miassolutions.milkledger.presentation.stats.AnalyticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val dataRepository: DataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()


    private var currentPeriod: Period = Period.DAILY
    private var currentRange: Pair<LocalDate, LocalDate> = Pair(LocalDate.now(), LocalDate.now())

    init {
        loadDaily()
    }



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

    /**
     * Load all records without date range limits.
     */
    private fun loadAllRecords() {
        viewModelScope.launch {
            combine(
                analyticsRepository.getTotalMilkPurchaseAll(),
                analyticsRepository.getTotalMilkSoldAll(),
                analyticsRepository.getTotalSalesAll(),
                analyticsRepository.getTotalPurchasesAll(),
                analyticsRepository.getTotalExpensesAll(),
                analyticsRepository.getProfitAll()
            ) { results ->
                val milkPurchase = results[0] ?: 0.0
                val milkSold = results[1] ?: 0.0
                val totalSales = results[2] ?: 0.0
                val totalPurchase = results[3] ?: 0.0
                val totalExpense = results[4] ?: 0.0
                val profit = results[5] as Double

                AnalyticsUiState(
                    milkPurchase = milkPurchase,
                    milkSold = milkSold,
                    salesTotal = totalSales,
                    purchaseTotal = totalPurchase,
                    expensesTotal = totalExpense,
                    profit = profit,
                    startDate = null,
                    endDate = null,
                    period = "All Records"
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    // --- Date Navigation ---
    fun onNextClicked() {
        shiftRange(+1)
    }

    fun onPrevClicked() {
        shiftRange(-1)
    }

    private fun shiftRange(direction: Int) {
        val (start, end) = currentRange
        val newRange = when (currentPeriod) {
            Period.DAILY -> Pair(start.plusDays(direction.toLong()), end.plusDays(direction.toLong()))
            Period.WEEKLY -> Pair(start.plusWeeks(direction.toLong()), end.plusWeeks(direction.toLong()))
            Period.MONTHLY -> Pair(start.plusMonths(direction.toLong()), end.plusMonths(direction.toLong()))
            Period.YEARLY -> Pair(start.plusYears(direction.toLong()), end.plusYears(direction.toLong()))
            Period.CUSTOM -> return // Skip shifting custom range
        }
        currentRange = newRange
        loadRange(newRange.first, newRange.second)
    }

    private fun loadRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            combine(
                analyticsRepository.getTotalMilkPurchaseBetween(start, end),
                analyticsRepository.getTotalMilkSoldBetween(start, end),
                analyticsRepository.getTotalSalesBetween(start, end),
                analyticsRepository.getTotalPurchasesBetween(start, end),
                analyticsRepository.getTotalExpensesBetween(start, end),
                analyticsRepository.getProfitBetween(start, end)
            ) { results ->
                val milkPurchase = results[0] ?: 0.0
                val milkSold = results[1] ?: 0.0
                val totalSales = results[2] ?: 0.0
                val totalPurchase = results[3] ?: 0.0
                val totalExpense = results[4] ?: 0.0
                val profit = results[5] as Double

                AnalyticsUiState(
                    milkPurchase = milkPurchase,
                    milkSold = milkSold,
                    salesTotal = totalSales,
                    purchaseTotal = totalPurchase,
                    expensesTotal = totalExpense,
                    profit = profit,
                    startDate = start,
                    endDate = end,
                    period = formatPeriodLabel(start, end)
                )
            }.collect {
                _uiState.value = it
            }
        }
    }


}
