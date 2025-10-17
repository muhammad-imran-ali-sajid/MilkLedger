package com.miassolutions.milkledger.presentation.dashboard


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ReportsRepository
) : ViewModel() {

    private val today = LocalDate.now()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val start = today
        val end = today

        combine(
            repository.getTotalSalesBetween(start, end),
            repository.getTotalPurchasesBetween(start, end),
            repository.getTotalExpensesBetween(start, end),
            repository.getProfitBetween(start, end)
        ) { sales, purchases, expenses, profit ->
            DashboardUiState(
                date = today,
                totalSales = sales ?: 0.0,
                totalPurchases = purchases ?: 0.0,
                totalExpenses = expenses ?: 0.0,
                profit = profit
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }
}
