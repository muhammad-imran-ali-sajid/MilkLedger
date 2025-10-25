package com.miassolutions.milkledger.presentation.customer.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.datesort.DateRangeHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType

import com.miassolutions.milkledger.data.repositories.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState = _uiState.asStateFlow()


    fun onSelectedCustomerId(id: String, name: String) {
        _uiState.update { it.copy(selectedCustomerId = id, customerName = name) }
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            val id = _uiState.value.selectedCustomerId ?: return@launch
            repository.getSalesForCustomer(id).collect { list ->
                val details = list.map { it.toCustomerDetail() }
                _uiState.update {
                    it.copy(
                        customerDetailList = details,
                        filteredList = details
                    )
                }
                filterData() // immediately filter after loading
            }
        }
    }


    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update {
            it.copy(
                selectedStartDate = start,
                selectedEndDate = end
            )
        }
        filterData()
    }


    private fun filterData() {
        val state = _uiState.value
        var filteredList = state.customerDetailList
        if (filteredList.isEmpty()) return

        viewModelScope.launch {
            val (startDate, endDate) = when {
                state.selectedStartDate != null && state.selectedEndDate != null ->
                    state.selectedStartDate to state.selectedEndDate

                else -> null to null
            }

            // 🔹 Apply Date Filter
            filteredList = filteredList.filter { detail ->
                detail.date in startDate!!..endDate!!
            }


            _uiState.update { it.copy(filteredList = filteredList) }
        }
    }

}
