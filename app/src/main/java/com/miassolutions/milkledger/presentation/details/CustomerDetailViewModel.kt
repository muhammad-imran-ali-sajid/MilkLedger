package com.miassolutions.milkledger.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.datesort.DateRangeHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.sort.FilterOptions
import com.miassolutions.milkledger.core.ui.sort.SortOrder
import com.miassolutions.milkledger.data.repositories.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: CustomerUiEvent) {
        when (event) {
            is CustomerUiEvent.ApplyFilter -> applyFilter(event.filter)
            is CustomerUiEvent.ChangeDateRange -> changeDateRange(event.rangeType)
        }
    }

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

    private fun applyFilter(filter: FilterOptions) {
        _uiState.update { it.copy(currentFilter = filter) }
        filterData()
    }

    private fun changeDateRange(rangeType: DateRangeType) {
        _uiState.update { it.copy(dateRangeType = rangeType) }
        filterData()
    }

    private fun filterData() {
        val state = _uiState.value
        val allDetails = state.customerDetailList
        if (allDetails.isEmpty()) return

        viewModelScope.launch {
            var filteredList = allDetails

            // 🔹 Step 1: Apply Date Range
            val (startDate, endDate) = DateRangeHelper.getRange(state.dateRangeType)
            filteredList = filteredList.filter { detail ->
                val date = detail.date
                date in startDate..endDate
            }

            // 🔹 Step 2: Apply Sorting Category
            state.currentFilter.category?.let { category ->
                filteredList = when (category) {
                    "Date" -> filteredList.sortedBy { it.date }
                    "Net Milk" -> filteredList.sortedBy { it.netMilk }
                    "Price" -> filteredList.sortedBy { it.milkPrice }
                    "Balance" -> filteredList.sortedBy { it.balance }
                    "Payment" -> filteredList.sortedBy { it.payment }
                    else -> filteredList
                }
            }

            // 🔹 Step 3: Apply Sort Order
            filteredList = when (state.currentFilter.sortOrder) {
                SortOrder.ASCENDING -> filteredList
                SortOrder.DESCENDING -> filteredList.reversed()
                else -> filteredList
            }

            _uiState.update { it.copy(filteredList = filteredList) }
        }
    }
}
