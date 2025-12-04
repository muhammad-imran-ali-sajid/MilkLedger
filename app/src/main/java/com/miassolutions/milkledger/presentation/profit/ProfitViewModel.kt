package com.miassolutions.milkledger.presentation.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.local.entities.ProfitEntity
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.mapper.toProfit
import com.miassolutions.milkledger.data.mapper.toProfitEntity
import com.miassolutions.milkledger.data.mapper.toProfitList
import com.miassolutions.milkledger.data.repository.ProfitRepository
import com.miassolutions.milkledger.domain.model.Profit
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class ProfitViewModel @Inject constructor(
    private val repository: ProfitRepository,
    private val sRepository: StatisticsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfitUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfitDetails()
    }

    val tp = sRepository.observeNetProfit()
    val _selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayNetProfit: StateFlow<Double> = _selectedDate

        .flatMapLatest { date ->
            repository.getNetBusinessProfitDaily(date)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)


    private val _todayProfit = MutableStateFlow(1000.0)
    val todayProfit = _todayProfit.asStateFlow()


    fun calculateProfit(date: LocalDate?) {

        if (date != null)
            viewModelScope.launch {
                val netProfit = repository.getNetBusinessProfitDaily(date).first()
                _todayProfit.value = netProfit

            }


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
//        viewModelScope.launch {
//            repository.getAllProfitList().collectLatest { list ->
//                val profitList = list.map { it.toProfit() }
//                val totalReceived = profitList.sumOf { it.receivedProfit }
//                val netProfit = repository.getNetProfit().first()
//
//                _uiState.value = _uiState.value.copy(
//                    netProfit = netProfit,
//                    profitList = profitList,
//                    filteredList = profitList,
//                    totalReceived = totalReceived,
//                    remainingProfit = netProfit - totalReceived,
//                    periodLabel = "All Records"
//                )
//            }
//        }
    }

    // -------------------------------------------------------------------------
    // DAILY
    // -------------------------------------------------------------------------
    private fun fetchDaily(date: LocalDate) {
        viewModelScope.launch {
            combine(
                repository.getDaily(date),                       // Flow<List<ProfitEntity>>
                repository.getNetBusinessProfitDaily(date),     // Flow<Double>
                repository.getProfitAfterPersonalExpenses(date) // Flow<Double?> or Flow<Double>
            ) { dailyList: List<ProfitEntity>, netBusinessProfit: Double, personalExpenses: Double? ->

                val pae = personalExpenses ?: 0.0

                val profits = dailyList.map { it.toProfitList(pae) } // returns List<ProfitListModel>
                val totalReceived = profits.sumOf { it.profitReceived }
                val remaining = netBusinessProfit - totalReceived

                ProfitDailyResult(
                    list = profits,
                    netBusinessProfit = netBusinessProfit,
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
                        remainingProfit = result.remainingProfit,
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
        val remainingProfit: Double
    )





    // -------------------------------------------------------------------------
    // WEEKLY
    // -------------------------------------------------------------------------
    private fun fetchWeekly(start: LocalDate, end: LocalDate) {
//        viewModelScope.launch {
//            val list = repository.getWeeklyWithNetProfit(start, end)
//
//            val totalReceived = list.sumOf { it.receivedProfit }
//            val netProfit = repository.getNetProfitWeekly(start, end)
//
//            _uiState.update {
//                it.copy(
//                    filteredList = list,
//                    totalReceived = totalReceived,
//                    netProfit = netProfit,
//                    remainingProfit = netProfit - totalReceived,
//                    periodLabel = formatPeriodLabel(start, end),
//                    startDate = start,
//                    endDate = end
//                )
//            }
//        }
    }

    // -------------------------------------------------------------------------
    // MONTHLY
    // -------------------------------------------------------------------------

    private fun fetchMonthly(yearMonth: YearMonth) {
//        viewModelScope.launch {
//
//            val start = yearMonth.atDay(1)
//            val end = yearMonth.atEndOfMonth()
//
//            // Repo expects a LocalDate or start/end – adjust as needed
//            val list = repository.getMonthly(start)
//            val profits = list.map { it.toProfit() }
//
//            val totalReceived = profits.sumOf { it.receivedProfit }
//
//            val netProfit = repository.getNetProfitMonthly(
//                yearMonth.year,
//                yearMonth.monthValue
//            )
//
//            // Custom label for Month + Year
//            val monthLabel = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
//
//
//            _uiState.update {
//                it.copy(
//                    filteredList = profits,
//                    totalReceived = totalReceived,
//                    netProfit = netProfit,
//                    remainingProfit = netProfit - totalReceived,
//                    periodLabel = monthLabel,
//                    startDate = start,
//                    endDate = end
//                )
//            }
//        }
    }


    // -------------------------------------------------------------------------
    // YEARLY
    // -------------------------------------------------------------------------
    private fun fetchYearly(year: Int) {
//        viewModelScope.launch {
//            val list = repository.getYearly(LocalDate.of(year, 1, 1))
//            val profits = list.map { it.toProfit() }
//
//            val totalReceived = profits.sumOf { it.receivedProfit }
//            val netProfit = repository.getNetProfitYearly(year)
//
//            _uiState.update {
//                it.copy(
//                    filteredList = profits,
//                    totalReceived = totalReceived,
//                    netProfit = netProfit,
//                    remainingProfit = netProfit - totalReceived,
//                    periodLabel = year.toString(),
//                    startDate = LocalDate.of(year, 1, 1),
//                    endDate = LocalDate.of(year, 12, 31)
//                )
//            }
//        }
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
//        viewModelScope.launch {
//            val list = repository.getCustom(start, end)
//            val profits = list.map { it.toProfit() }
//
//            val totalReceived = profits.sumOf { it.receivedProfit }
//            val netProfit = repository.getNetProfitCustom(start, end)
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
    }

    // -------------------------------------------------------------------------
    // SAVE / DELETE
    // -------------------------------------------------------------------------
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

    fun getProfit(id: String, onResult: (Profit?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.getProfitById(id)?.toProfit())
        }
    }
}
