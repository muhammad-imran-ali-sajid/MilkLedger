package com.miassolutions.milkledger.presentation.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.getTotalChangedRows
import com.miassolutions.milkledger.data.mapper.toProfit
import com.miassolutions.milkledger.data.mapper.toProfitEntity
import com.miassolutions.milkledger.data.repository.ProfitRepository
import com.miassolutions.milkledger.domain.model.Profit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfitViewModel @Inject constructor(
    private val repository: ProfitRepository

) : ViewModel() {


    private val _uiState = MutableStateFlow(ProfitUiState())
    val uiState = _uiState.asStateFlow()


    init {
        loadProfitList()
    }


    private fun loadProfitList() {
        viewModelScope.launch {
            repository.getAllProfitList()
                .collect { list ->
                    val profitList = list.map { it.toProfit() }

                    _uiState.update { state ->
                        val totalReceived = profitList.sumOf { it.receivedProfit }
                        val netProfit = repository.getNetProfit().first()

                        state.copy(
                            profitList = profitList,
                            filteredList = profitList,
                            netProfit = netProfit,
                            totalReceived = totalReceived,
                            remainingProfit = netProfit - totalReceived

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