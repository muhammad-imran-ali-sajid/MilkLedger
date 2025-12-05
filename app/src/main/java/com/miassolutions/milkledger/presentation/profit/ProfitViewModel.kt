package com.miassolutions.milkledger.presentation.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.mapper.toProfitList
import com.miassolutions.milkledger.data.repository.ProfitRepository
import com.miassolutions.milkledger.presentation.datefilter.DatePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
class ProfitViewModel @Inject constructor(
    private val repository: ProfitRepository,
    private val sRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfitUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())


    @OptIn(ExperimentalCoroutinesApi::class)
    val profitAfterPersonal: StateFlow<Double> = _selectedDate
        .flatMapLatest { date ->
            repository.getProfitAfterPersonalExpenses(date)
        }
        .map { it ?: 0.0 }   // <-- FIX: handle null values
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    init {
        loadProfitDetails()
    }

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // ------------------------------------------------------------------------------
    // LIVE TODAY BUSINESS PROFIT
    // ------------------------------------------------------------------------------
    @OptIn(ExperimentalCoroutinesApi::class)
    val todayNetProfit: StateFlow<Double> =
        _selectedDate.flatMapLatest { date ->
            repository.getNetBusinessProfitDaily(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    fun calculateProfit(date: LocalDate?) {
        if (date == null) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    netBusinessProfit = repository.getNetBusinessProfitDaily(date).first()
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

    private fun fetchAll() {
        viewModelScope.launch {
            repository.getAllNetProfit().collect { entities ->

                val businessNet = repository.getNetProfitOnce()
                val personal =
                    0.0  // For ALL records you currently don't subtract personal expenses

                val profits = entities.map { it.toProfitList(personal) }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = businessNet - totalReceived

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        netBusinessProfit = businessNet,
                        netProfitAfterPersonalExpenses = personal,
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
    // INITIAL ALL RECORDS LOAD
    // ------------------------------------------------------------------------------
    private fun loadProfitDetails() {
        viewModelScope.launch {

            combine(
                repository.getAllNetProfit(),
                repository.getNetBusinessProfit(),
                repository.getNetProfitAfterPersonal()
            ){ netList, businessNet, profitAfterPersonal ->

                val pae = profitAfterPersonal

                val sorted = netList.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList(pae) }

                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = profitAfterPersonal.minus(totalReceived)

                ProfitDailyResult(
                    list = profits,
                    netBusinessProfit = businessNet,
                    personalExpenses = pae,
                    totalReceived = totalReceived,
                    remainingProfit = remaining
                )
            }.collect { result ->
                _uiState.update {
                    it.copy(
                        filteredList = result.list,
                        netBusinessProfit = result.netBusinessProfit,
                        netProfitAfterPersonalExpenses = result.personalExpenses,
                        totalReceived = result.totalReceived,
                        remainingProfit = result.remainingProfit ?: 0.0,
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
            combine(
                repository.getDaily(date),
                repository.getNetBusinessProfitDaily(date),
                repository.getProfitAfterPersonalExpenses(date)
            ) { dailyList, businessNet, profitAfterPersonal ->

                val pae = profitAfterPersonal ?: 0.0

                val sorted = dailyList.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList(pae) }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = profitAfterPersonal?.minus(totalReceived)

                ProfitDailyResult(
                    list = profits,
                    netBusinessProfit = businessNet,
                    personalExpenses = pae,
                    totalReceived = totalReceived,
                    remainingProfit = remaining
                )
            }.collect { result ->
                _uiState.update {
                    it.copy(
                        filteredList = result.list,
                        netBusinessProfit = result.netBusinessProfit,
                        netProfitAfterPersonalExpenses = result.personalExpenses,
                        totalReceived = result.totalReceived,
                        remainingProfit = result.remainingProfit ?: 0.0,
                        periodLabel = formatPeriodLabel(date, date),
                        startDate = date,
                        endDate = date
                    )
                }
            }
        }
    }

    private data class ProfitDailyResult(
        val list: List<ProfitListModel>,
        val netBusinessProfit: Double,
        val personalExpenses: Double,
        val totalReceived: Double,
        val remainingProfit: Double?
    )

    // ------------------------------------------------------------------------------
    // WEEKLY
    // ------------------------------------------------------------------------------
    private fun fetchWeekly(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            repository.getWeekly(start, end).collect { entities ->

                val businessNet = repository.getNetProfitWeekly(start, end)
                val personal =
                    repository.getProfitAfterPersonalExpensesBetween(start, end).first() ?: 0.0

                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList(personal) }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = businessNet - totalReceived

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        netBusinessProfit = businessNet,
                        netProfitAfterPersonalExpenses = personal,
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

            repository.getMonthly(start).collect { entities ->
                val businessNet =
                    repository.getNetProfitMonthly(yearMonth.year, yearMonth.monthValue)
                val personal =
                    repository.getProfitAfterPersonalExpensesBetween(start, end).first() ?: 0.0
                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList(personal) }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = businessNet - totalReceived

                val label = "${yearMonth.month} ${yearMonth.year}"

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        netBusinessProfit = businessNet,
                        netProfitAfterPersonalExpenses = personal,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = label,
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

            repository.getYearly(start).collect { entities ->
                val businessNet = repository.getNetProfitYearly(year)
                val personal =
                    repository.getProfitAfterPersonalExpensesBetween(start, end).first() ?: 0.0

                val sorted = entities.sortedByDescending { it.receivedDate }
                val profits = sorted.map { it.toProfitList(personal) }
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = businessNet - totalReceived

                _uiState.update {
                    it.copy(
                        filteredList = profits,
                        netBusinessProfit = businessNet,
                        netProfitAfterPersonalExpenses = personal,
                        totalReceived = totalReceived,
                        remainingProfit = remaining,
                        periodLabel = year.toString(),
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
            val entities = repository.getCustom(start, end)
            val businessNet = repository.getNetProfitCustom(start, end)
            val personal =
                repository.getProfitAfterPersonalExpensesBetween(start, end).first() ?: 0.0

            val sorted = entities.sortedByDescending { it.receivedDate }
            val profits = sorted.map { it.toProfitList(personal) }
            val totalReceived = profits.sumOf { it.profitReceived }
            val remaining = businessNet - totalReceived

            _uiState.update {
                it.copy(
                    filteredList = profits,
                    netBusinessProfit = businessNet,
                    netProfitAfterPersonalExpenses = personal,
                    totalReceived = totalReceived,
                    remainingProfit = remaining,
                    periodLabel = formatPeriodLabel(start, end),
                    startDate = start,
                    endDate = end
                )
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
