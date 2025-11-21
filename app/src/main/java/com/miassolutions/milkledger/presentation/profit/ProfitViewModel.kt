package com.miassolutions.milkledger.presentation.profit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.mapper.toProfit
import com.miassolutions.milkledger.data.repository.ProfitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
                        val totalProfit = profitList.sumOf { it.receivedProfit }

                        state.copy(
                            profitList = profitList,
                            filteredList = profitList,
                            totalProfit = totalProfit
                        )
                    }
                }
        }
    }


}