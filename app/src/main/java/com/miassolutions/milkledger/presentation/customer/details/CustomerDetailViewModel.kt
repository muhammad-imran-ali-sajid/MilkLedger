package com.miassolutions.milkledger.presentation.customer.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.datesort.DateRangeHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.sort.FilterOptions
import com.miassolutions.milkledger.core.ui.sort.SortOrder
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

    fun onEvent(event: CustomerUiEvent) {
        when (event) {
            is CustomerUiEvent.ApplyFilter -> applyFilter(event.filter)
            is CustomerUiEvent.ChangeDateRange -> changeDateRange(event.rangeType)
            is CustomerUiEvent.NextButton -> moveDateRange(forward = true)
            is CustomerUiEvent.PrevButton -> moveDateRange(forward = false)
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

    fun changeDateRange(
        rangeType: DateRangeType,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ) {
        _uiState.update {
            it.copy(
                dateRangeType = rangeType,
                selectedStartDate = startDate,
                selectedEndDate = endDate
            )
        }
        filterData()
    }

    private fun moveDateRange(forward: Boolean) {
        val multiplier = if (forward) 1 else -1
        val state = _uiState.value

        val newStart: LocalDate
        val newEnd: LocalDate

        when (state.dateRangeType) {
            DateRangeType.TODAY -> {
                val base = state.selectedStartDate ?: LocalDate.now()
                newStart = base.plusDays(multiplier.toLong())
                newEnd = newStart
            }

            DateRangeType.WEEK -> {
                val base = state.selectedStartDate ?: LocalDate.now()
                newStart = base.plusWeeks(multiplier.toLong())
                newEnd = newStart.plusDays(6)
            }

            DateRangeType.MONTH -> {
                val base = state.selectedStartDate ?: LocalDate.now().withDayOfMonth(1)
                newStart = base.plusMonths(multiplier.toLong())
                newEnd = newStart.withDayOfMonth(newStart.lengthOfMonth())
            }

            else -> return // Don't support CUSTOM or UNKNOWN range for prev/next
        }

        _uiState.update {
            it.copy(
                selectedStartDate = newStart,
                selectedEndDate = newEnd
            )
        }
        filterData()
    }


    fun changeDateRange(rangeType: DateRangeType) {
        _uiState.update { it.copy(dateRangeType = rangeType) }
        filterData()
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
                else -> DateRangeHelper.getRange(state.dateRangeType)
            }

            // 🔹 Apply Date Filter
            filteredList = filteredList.filter { detail ->
                detail.date in startDate!!..endDate!!
            }

            // 🔹 Apply Sorting
            state.currentFilter.category?.let { category ->
                filteredList = when (category) {
                    "Name" -> filteredList.sortedBy { it.date }
                    "Net Milk" -> filteredList.sortedBy { it.netMilk }
                    else -> filteredList
                }
            }

            filteredList = when (state.currentFilter.sortOrder) {
                SortOrder.ASCENDING -> filteredList
                SortOrder.DESCENDING -> filteredList.reversed()
                else -> filteredList
            }

            _uiState.update { it.copy(filteredList = filteredList) }
        }
    }

}
