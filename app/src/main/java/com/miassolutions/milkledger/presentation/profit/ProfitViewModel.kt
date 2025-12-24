package com.miassolutions.milkledger.presentation.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.extensions.formatPeriodLabel
import com.miassolutions.milkledger.data.oldmapper.toEntity
import com.miassolutions.milkledger.data.oldmapper.toProfitList
import com.miassolutions.milkledger.data.repository.ProfitRepository
import com.miassolutions.milkledger.presentation.datefilter.DatePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ProfitViewModel @Inject constructor(
    private val repository: ProfitRepository,
    private val sRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfitUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())


    val todayGrossProfit: StateFlow<Double> = _selectedDate
        .flatMapLatest { date ->
            repository.getGrossProfitDaily(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    val todayNetProfit: StateFlow<Double> = _selectedDate
        .flatMapLatest { date ->
            repository.getNetProfitDaily(date)
        }
        .map { it ?: 0.0 }   // <-- FIX: handle null values
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)




    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // ------------------------------------------------------------------------------
    // LIVE TODAY BUSINESS PROFIT
    // ------------------------------------------------------------------------------


    fun calculateProfit(date: LocalDate?) {
        if (date == null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    grossProfit = repository.getGrossProfitDaily(date).first()
                )
            }
        }
    }

    // ------------------------------------------------------------------------------
    // ROUTER
    // ------------------------------------------------------------------------------
    fun loadData(period: DatePeriod) {
        when (period) {
            is DatePeriod.Daily -> fetchDaily(period.date)
            is DatePeriod.Weekly -> fetchWeekly(period.start, period.end)
            is DatePeriod.Monthly -> fetchMonthly(period.month)
            is DatePeriod.Yearly -> fetchYearly(period.year)
            DatePeriod.All -> loadProfitDetails()
            is DatePeriod.Custom -> fetchCustom(period.start, period.end)

        }
    }


    // ------------------------------------------------------------------------------
    // INITIAL ALL RECORDS LOAD
    // ------------------------------------------------------------------------------
    private fun loadProfitDetails() {
        viewModelScope.launch {


            repository.getAllNetProfit().collect { entities ->

                val sorted = entities.sortedByDescending { p -> p.receivedDate }
                val profits = sorted.map { it.toProfitList() }

                val netProfit = entities.sumOf { it.netProfit }
                val grossProfit = entities.sumOf { it.grossProfit }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = netProfit.minus(totalReceived)


                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        grossProfit = grossProfit,
                        netProfit = netProfit,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = "All Records",
                        startDate = null,
                        endDate = null
                    )
                }
            }


        }
    }

    // ------------------------------------------------------------------------------
    // DAILY (Already Working)
    // ------------------------------------------------------------------------------
    private fun fetchDaily(date: LocalDate) {
        viewModelScope.launch {
            repository.getDailyReceivedProfit(date).collect { entities ->

                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList() }

                val netProfit = entities.sumOf { it.netProfit }
                val grossProfit = entities.sumOf { it.grossProfit }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = netProfit.minus(totalReceived)

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        grossProfit = grossProfit,
                        netProfit = netProfit,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = formatPeriodLabel(date, date),
                        startDate = date,
                        endDate = date
                    )
                }
            }
        }
    }


    // ------------------------------------------------------------------------------
    // WEEKLY
    // ------------------------------------------------------------------------------
    private fun fetchWeekly(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {

            repository.getReceivedProfitBetween(start, end).collect { entities ->


                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList() }

                val netProfit = entities.sumOf { it.netProfit }
                val grossProfit = entities.sumOf { it.grossProfit }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = netProfit.minus(totalReceived)

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        grossProfit = grossProfit,
                        netProfit = netProfit,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = formatPeriodLabel(start, end),
                        startDate = start,
                        endDate = end
                    )
                }
            }

        }
    }

    // ------------------------------------------------------------------------------
    // MONTHLY
    // ------------------------------------------------------------------------------
    private fun fetchMonthly(yearMonth: YearMonth) {
        viewModelScope.launch {
            val start = yearMonth.atDay(1)
            val end = yearMonth.atEndOfMonth()

            repository.getReceivedProfitBetween(start, end).collect { entities ->


                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList() }

                val netProfit = entities.sumOf { it.netProfit }
                val grossProfit = entities.sumOf { it.grossProfit }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = netProfit.minus(totalReceived)

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        grossProfit = grossProfit,
                        netProfit = netProfit,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = formatPeriodLabel(start, end),
                        startDate = start,
                        endDate = end
                    )
                }
            }

        }
    }

    // ------------------------------------------------------------------------------
    // YEARLY
    // ------------------------------------------------------------------------------
    private fun fetchYearly(year: Int) {
        viewModelScope.launch {
            val start = LocalDate.of(year, 1, 1)
            val end = LocalDate.of(year, 12, 31)

            repository.getReceivedProfitBetween(start, end).collect { entities ->


                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList() }

                val netProfit = entities.sumOf { it.netProfit }
                val grossProfit = entities.sumOf { it.grossProfit }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = netProfit.minus(totalReceived)

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        grossProfit = grossProfit,
                        netProfit = netProfit,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = formatPeriodLabel(start, end),
                        startDate = start,
                        endDate = end
                    )
                }
            }

        }
    }

    // ------------------------------------------------------------------------------
    // CUSTOM
    // ------------------------------------------------------------------------------
    private fun fetchCustom(start: LocalDate?, end: LocalDate?) {
        if (start == null || end == null) {
            loadProfitDetails()
            return
        }
        loadRange(start, end)
    }

    private fun loadRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            repository.getReceivedProfitBetween(start, end).collect { entities ->


                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList() }

                val netProfit = entities.sumOf { it.netProfit }
                val grossProfit = entities.sumOf { it.grossProfit }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = netProfit.minus(totalReceived)

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        grossProfit = grossProfit,
                        netProfit = netProfit,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = formatPeriodLabel(start, end),
                        startDate = start,
                        endDate = end
                    )
                }
            }

        }
    }

    // ------------------------------------------------------------------------------
    // SAVE / DELETE
    // ------------------------------------------------------------------------------
    fun saveProfit(profit: ProfitListModel) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.upsert(profit.toEntity())
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteProfit(profitId: String) {
        viewModelScope.launch {
            try {
                repository.deleteProfit(profitId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
