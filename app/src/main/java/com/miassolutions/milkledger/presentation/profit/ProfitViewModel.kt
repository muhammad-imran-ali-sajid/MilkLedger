package com.miassolutions.milkledger.presentation.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.mapper.toProfit
import com.miassolutions.milkledger.data.mapper.toProfitEntity
import com.miassolutions.milkledger.data.repository.ProfitRepository
import com.miassolutions.milkledger.domain.model.Profit
import com.miassolutions.milkledger.presentation.datefilter.DatePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ProfitViewModel @Inject constructor(
    private val repository: ProfitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfitUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfitDetails()
    }

    private val _todayProfit = MutableStateFlow(1000.0)
    val todayProfit = _todayProfit.asStateFlow()

    fun calculateProfit(date: LocalDate?) {

        if (date != null)
            viewModelScope.launch {
                repository.getProfitToday(date = date).collectLatest { d ->
                    d?.let { _todayProfit.value = it }
                }
            }.start()


    }

    // -------------------------------------------------------------------------
    // MAIN ENTRY — Called by your date filter
    // -------------------------------------------------------------------------
    fun loadData(period: DatePeriod) {
        when (period) {
            is DatePeriod.Daily -> fetchDaily(period.date)
            is DatePeriod.Weekly -> fetchWeekly(period.start, period.end)
            is DatePeriod.Monthly -> fetchMonthly(period.month)
            is DatePeriod.Yearly -> fetchYearly(period.year)
            DatePeriod.All -> fetchAll()
            is DatePeriod.Custom -> fetchCustom(period.start, period.end)

        }
    }

    // -------------------------------------------------------------------------
    // INITIAL LOAD
    // -------------------------------------------------------------------------
    private fun loadProfitDetails() {
        viewModelScope.launch {
            repository.getAllProfitList().collectLatest { list ->
                val profitList = list.map { it.toProfit() }
                val totalReceived = profitList.sumOf { it.receivedProfit }
                val netProfit = repository.getNetProfit().first()

                _uiState.value = _uiState.value.copy(
                    netProfit = netProfit,
                    profitList = profitList,
                    filteredList = profitList,
                    totalReceived = totalReceived,
                    remainingProfit = netProfit - totalReceived,
                    periodLabel = "All Records"
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // DAILY
    // -------------------------------------------------------------------------
    private fun fetchDaily(date: LocalDate) {
        viewModelScope.launch {
            val list = repository.getDaily(date)
            val netProfit = repository.getNetProfitDaily(date).first()

            val profits = list.map { it.toProfit() }

            val totalReceived = profits.sumOf { it.receivedProfit }

            _uiState.update {
                it.copy(
                    filteredList = profits,
                    totalReceived = totalReceived,
                    netProfit = netProfit,
                    remainingProfit = netProfit - totalReceived,
                    periodLabel = formatPeriodLabel(date, date),
                    startDate = date,
                    endDate = date
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // WEEKLY
    // -------------------------------------------------------------------------
    private fun fetchWeekly(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            val list = repository.getWeeklyWithNetProfit(start, end)

            val totalReceived = list.sumOf { it.receivedProfit }
            val netProfit = repository.getNetProfitWeekly(start, end)

            _uiState.update {
                it.copy(
                    filteredList = list,
                    totalReceived = totalReceived,
                    netProfit = netProfit,
                    remainingProfit = netProfit - totalReceived,
                    periodLabel = formatPeriodLabel(start, end),
                    startDate = start,
                    endDate = end
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // MONTHLY
    // -------------------------------------------------------------------------

    private fun fetchMonthly(yearMonth: YearMonth) {
        viewModelScope.launch {

            val start = yearMonth.atDay(1)
            val end = yearMonth.atEndOfMonth()

            // Repo expects a LocalDate or start/end – adjust as needed
            val list = repository.getMonthly(start)
            val profits = list.map { it.toProfit() }

            val totalReceived = profits.sumOf { it.receivedProfit }

            val netProfit = repository.getNetProfitMonthly(
                yearMonth.year,
                yearMonth.monthValue
            )

            // Custom label for Month + Year
            val monthLabel = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))


            _uiState.update {
                it.copy(
                    filteredList = profits,
                    totalReceived = totalReceived,
                    netProfit = netProfit,
                    remainingProfit = netProfit - totalReceived,
                    periodLabel = monthLabel,
                    startDate = start,
                    endDate = end
                )
            }
        }
    }


//    private fun fetchMonthly(month: LocalDate) {
//        viewModelScope.launch {
//            val list = repository.getMonthly(month)
//            val profits = list.map { it.toProfit() }
//
//            val totalReceived = profits.sumOf { it.receivedProfit }
//            val netProfit = repository.getNetProfitMonthly(month.year, month.monthValue)
//
//            val start = month.withDayOfMonth(1)
//            val end = month.withDayOfMonth(month.lengthOfMonth())
//
//            _uiState.update {
//                it.copy(
//                    filteredList = profits,
//                    totalReceived = totalReceived,
//                    netProfit = netProfit,
//                    remainingProfit = netProfit - totalReceived,
//                    periodLabel = formatPeriodLabel(start, end),
//                    startDate = start,
//                    endDate = end
//                )
//            }
//        }
//    }

    // -------------------------------------------------------------------------
    // YEARLY
    // -------------------------------------------------------------------------
    private fun fetchYearly(year: Int) {
        viewModelScope.launch {
            val list = repository.getYearly(LocalDate.of(year, 1, 1))
            val profits = list.map { it.toProfit() }

            val totalReceived = profits.sumOf { it.receivedProfit }
            val netProfit = repository.getNetProfitYearly(year)

            _uiState.update {
                it.copy(
                    filteredList = profits,
                    totalReceived = totalReceived,
                    netProfit = netProfit,
                    remainingProfit = netProfit - totalReceived,
                    periodLabel = year.toString(),
                    startDate = LocalDate.of(year, 1, 1),
                    endDate = LocalDate.of(year, 12, 31)
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // ALL RECORDS
    // -------------------------------------------------------------------------
    private fun fetchAll() = loadProfitDetails()

    // -------------------------------------------------------------------------
    // CUSTOM
    // -------------------------------------------------------------------------
    private fun fetchCustom(start: LocalDate?, end: LocalDate?) {
        if (start == null || end == null) {
            loadProfitDetails()
            return
        }
        loadRange(start, end)
    }

    private fun loadRange(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            val list = repository.getCustom(start, end)
            val profits = list.map { it.toProfit() }

            val totalReceived = profits.sumOf { it.receivedProfit }
            val netProfit = repository.getNetProfitCustom(start, end)

            _uiState.update {
                it.copy(
                    filteredList = profits,
                    totalReceived = totalReceived,
                    netProfit = netProfit,
                    remainingProfit = netProfit - totalReceived,
                    periodLabel = formatPeriodLabel(start, end),
                    startDate = start,
                    endDate = end
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // SAVE / DELETE
    // -------------------------------------------------------------------------
    fun saveProfit(profit: Profit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                repository.upsert(profit.toProfitEntity())
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteProfit(profit: Profit) {
        viewModelScope.launch {
            try {
                repository.delete(profit.toProfitEntity())
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun getProfit(id: String, onResult: (Profit?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getProfitById(id)?.toProfit())
        }
    }
}
