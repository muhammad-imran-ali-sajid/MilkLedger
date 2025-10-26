package com.miassolutions.milkledger.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.DateRangeUtil
import com.miassolutions.milkledger.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    fun loadWeeklyData() = viewModelScope.launch {
        val (start, end) = DateRangeUtil.thisWeek()
        val sales = analyticsRepository.getSalesTotal(start, end)
        val purchases = analyticsRepository.getPurchasesTotal(start, end)
        val profit = analyticsRepository.getProfit(start, end)
        _uiState.value = AnalyticsUiState(
            period = "This Week",
            salesTotal = sales,
            purchaseTotal = purchases,
            profit = profit
        )
    }
}


