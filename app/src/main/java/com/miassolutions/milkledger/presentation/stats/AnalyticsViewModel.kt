package com.miassolutions.milkledger.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.DateRangeUtil
import com.miassolutions.milkledger.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    // ---------- PUBLIC LOADERS ----------

    fun loadWeeklyData() {
        loadDataForRange("This Week", DateRangeUtil.thisWeek())
    }

    fun loadMonthlyData() {
        loadDataForRange("This Month", DateRangeUtil.thisMonth())
    }

    fun loadYearlyData() {
        loadDataForRange("This Year", DateRangeUtil.thisYear())
    }

    fun loadCustomData(start: LocalDate, end: LocalDate) {
        loadDataForRange("Custom Range", Pair(start, end))
    }

    // ---------- CORE LOADER ----------
    // ... (inside AnalyticsViewModel)

    // ---------- CORE LOADER ----------
    private fun loadDataForRange(periodLabel: String, range: Pair<LocalDate, LocalDate>) {

        val (start, end) = range

        // Use the combine overload for 6 flows (results array)
        combine(
//            analyticsRepository.getTotalMilkPurchaseBetween(start, end), // 1st element
//            analyticsRepository.getTotalMilkSoldBetween(start, end),     // 2nd element
//            analyticsRepository.getTotalSalesBetween(start, end),       // 3rd element
//            analyticsRepository.getTotalPurchasesBetween(start, end),   // 4th element
//            analyticsRepository.getTotalExpensesBetween(start, end),    // 5th element
//            analyticsRepository.getProfitBetween(start, end)            // 6th element
        ) { results ->

            // Fix: Use a run block to manage variable assignment and casting cleanly
            // Destructuring here would require a manually-written componentN function for the Array
            // So, we just declare and assign variables explicitly for clarity:

            val milkPurchase = results[0] ?: 0.0
            val milkSold = results[1] ?: 0.0
            val totalSales = results[2] ?: 0.0
            val totalPurchase = results[3] ?: 0.0
            val totalExpense = results[4] ?: 0.0
            val netProfit = results[5] as Double // Already non-nullable from repo combine

            AnalyticsUiState(
                period = periodLabel,
                milkPurchase = milkPurchase,
                milkSold = milkSold,
                salesTotal = totalSales,
                purchaseTotal = totalPurchase,
                fixedExpense = totalExpense,
                profit = netProfit,
                startDate = start,
                endDate = end
            )

        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
