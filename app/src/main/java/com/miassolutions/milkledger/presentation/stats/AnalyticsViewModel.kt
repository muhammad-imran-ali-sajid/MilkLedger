package com.miassolutions.milkledger.presentation.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.DateRangeUtil
import com.miassolutions.milkledger.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private fun loadDataForRange(periodLabel: String, range: Pair<LocalDate, LocalDate>) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val (start, end) = range

                val sales = analyticsRepository.getSalesTotal(start, end)
                Log.d("AnalyticalViewModel", "$sales")
                val purchases = analyticsRepository.getPurchasesTotal(start, end)
                val profit = analyticsRepository.getProfit(start, end)

                _uiState.value = _uiState.value.copy(
                    period = periodLabel,
                    startDate = start,
                    endDate = end,
                    salesTotal = sales,
                    purchaseTotal = purchases,
                    profit = profit,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                e.printStackTrace()
            }
        }
    }
}


