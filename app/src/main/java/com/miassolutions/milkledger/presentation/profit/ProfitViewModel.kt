package com.miassolutions.milkledger.presentation.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.getTotalChangedRows
import com.miassolutions.milkledger.core.util.formatPeriodLabel
import com.miassolutions.milkledger.data.mapper.toProfit
import com.miassolutions.milkledger.data.mapper.toProfitEntity
import com.miassolutions.milkledger.data.repository.ProfitRepository
import com.miassolutions.milkledger.domain.model.Profit
import com.miassolutions.milkledger.presentation.dashboard.DashboardViewModel.Period
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
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

    private fun loadProfitDetails() {
        viewModelScope.launch {
            repository.getAllProfitList()
                .collect { list ->
                    val profitList = list.map { it.toProfit() }

                    val totalReceived = profitList.sumOf { it.receivedProfit }
                    val netProfit = repository.getNetProfit().first()

                    _uiState.update { state ->
                        state.copy(
                            profitList = profitList,
                            filteredList = profitList,
                            totalReceived = totalReceived,
                            remainingProfit = netProfit - totalReceived,
                            periodLabel = "All Records"
                        )
                    }
                }
        }
    }


    // ------------------------------------------------------------
    // ADD OR UPDATE PROFIT
    // ------------------------------------------------------------
    fun saveProfit(profit: Profit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                repository.upsert(profit.toProfitEntity())

                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun loadRange(start: LocalDate?, end: LocalDate?) {
        val state = _uiState.value
        val filteredList = state.profitList

        if (filteredList.isEmpty()) return

        viewModelScope.launch {

            var currentFilterList = filteredList

            if (start != null && end != null) {

                val periodLabel = getFormattedDateRange(start, end)

                currentFilterList = filteredList.filter { profit ->
                    profit.receivedDate in start..end
                }

                val totalReceived = currentFilterList.sumOf { it.receivedProfit }
                val netProfit = repository.getNetProfit().first()

                _uiState.update { it.copy(
                    totalReceived = totalReceived,
                    netProfit = netProfit,
                    filteredList = currentFilterList,
                    remainingProfit = netProfit - totalReceived,
                    periodLabel = periodLabel,
                    startDate = start,
                    endDate = end
                )}
            }



        }
    }

    fun getFormattedDateRange(start: LocalDate?, end: LocalDate?): String {
        return when {
            start != null && end != null -> {
                formatPeriodLabel(start, end)
            }

            else -> "All Records"
        }
    }


    fun loadCustom(start: LocalDate?, end: LocalDate?) {

        if (start == null || end == null) {

            loadProfitDetails()
        } else {
            loadRange(start, end)
        }
    }


    // ------------------------------------------------------------
    // DELETE PROFIT
    // ------------------------------------------------------------
    fun deleteProfit(profit: Profit) {
        viewModelScope.launch {
            val profitEntity = profit.toProfitEntity()
            try {
                repository.delete(profitEntity)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    // ------------------------------------------------------------
    // GET SINGLE PROFIT (for Edit)
    // ------------------------------------------------------------
    fun getProfit(id: String, onResult: (Profit?) -> Unit) {
        viewModelScope.launch {
            val entity = repository.getProfitById(id)
            onResult(entity?.toProfit())
        }
    }

    // ------------------------------------------------------------
    // FILTER LIST (optional)
    // ------------------------------------------------------------
//    fun filterByDate(date: Long) {
//        val original = _uiState.value.profitList
//
//        val filtered = original.filter { it.receivedDate == date }
//
//        _uiState.update {
//            it.copy(filteredList = filtered)
//        }
//    }
//
//    fun clearFilter() {
//        _uiState.update { state ->
//            state.copy(filteredList = state.profitList)
//        }
//    }


}