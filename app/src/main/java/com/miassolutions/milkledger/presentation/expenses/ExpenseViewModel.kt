package com.miassolutions.milkledger.presentation.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.local.entities.ExpensesEntity
import com.miassolutions.milkledger.data.repository.ExpensesRepository
import com.miassolutions.milkledger.presentation.datefilter.DatePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpensesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpensesUiState())
    val uiState = _uiState.asStateFlow()

    init {
//        loadForDate(LocalDate.now())

        fetchDaily(LocalDate.now())
    }

    fun onEvent(event: ExpensesUiEvent) {
        when (event) {

            is ExpensesUiEvent.SelectDate -> {
                // Update the state and trigger data collection for the newly selected date
                _uiState.update { it.copy(currentDate = event.date) }
//                collectExpenses(event.date)
                fetchDaily(event.date)
            }
        }

    }


    // ADD MODE → Save multiple new expenses at once
    fun saveExpenses(list: List<ExpensesEntity>) = viewModelScope.launch {
        repository.upsertAllExpenses(list)
    }

    // EDIT MODE → Update single expense
    fun updateExpense(entry: ExpensesEntity) = viewModelScope.launch {
        repository.upsertExpense(entry)
    }


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
    // DAILY
    // -------------------------------------------------------------------------
    private fun fetchDaily(date: LocalDate) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }


            repository.getDailyExpenses(date).collect { list ->

                val totalExpenses = list.sumOf { it.expenseAmount }
                val personalList = list.filter { entity -> entity.isDefault }
                val netBusinessExpenses = personalList.sumOf { it.expenseAmount }


                _uiState.update {
                    it.copy(
                        filteredList = list,
                        businessTotalExpenses = netBusinessExpenses,
                        personalTotalExpenses = totalExpenses - netBusinessExpenses,
                        periodLabel = formatPeriodLabel(date, date),
                        startDate = date,
                        endDate = date,
                        isLoading = false

                    )
                }
            }


        }
    }

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
        viewModelScope.launch {

            val start = yearMonth.atDay(1)
            val end = yearMonth.atEndOfMonth()

            // Repo expects a LocalDate or start/end – adjust as needed
            val list = repository.getMonthlyExpenses(start)

            val totalExpenses = list.sumOf { it.expenseAmount }
            val personalList = list.filter { entity -> entity.isDefault }
            val netBusinessExpenses = personalList.sumOf { it.expenseAmount }

            // Custom label for Month + Year
            val monthLabel = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))


            _uiState.update {
                it.copy(
                    filteredList = list,
                    businessTotalExpenses = netBusinessExpenses,
                    personalTotalExpenses = totalExpenses - netBusinessExpenses,
                    periodLabel = monthLabel,
                    startDate = start,
                    endDate = end,
                    isLoading = false
                )
            }
        }
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
    private fun fetchAll() {
//        loadProfitDetails()
    }

    // -------------------------------------------------------------------------
    // CUSTOM
    // -------------------------------------------------------------------------
    private fun fetchCustom(start: LocalDate?, end: LocalDate?) {
//        if (start == null || end == null) {
//            loadProfitDetails()
//            return
//        }
//        loadRange(start, end)
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

//    fun onEvent(event: ExpensesUiEvent) {
//        when (event) {
//            is ExpensesUiEvent.SelectDate -> loadForDate(event.date)
//        }
//    }

//    private fun loadForDate(date: LocalDate) {
//
//        // Update date in UiState
//        _uiState.update { it.copy(currentDate = date, isLoading = true) }
//
//        viewModelScope.launch {
////            ensureDefaultExpenses(date)
//        }
//
//        collectFixedExpenses(date)
//    }


    // --------------------------------------------------
    // Separate collectors make UI simpler
    // --------------------------------------------------
//    private fun collectFixedExpenses(date: LocalDate) {
//        viewModelScope.launch {
//            repository.getFixedExpenses(date).collect { list ->
//                val total = list.sumOf { it.expenseAmount }
//
//                _uiState.update {
//                    it.copy(
//                        businessExpenses = list,
//                        businessTotalExpenses = total,
//                        isLoading = false
//
//                    )
//                }
//            }
//        }
//    }


    fun deleteExpense(expense: ExpensesEntity) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }


}
