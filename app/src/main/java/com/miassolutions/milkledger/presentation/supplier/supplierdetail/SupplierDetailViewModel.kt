package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.datesort.DateRangeHelper
import com.miassolutions.milkledger.core.ui.datesort.DateRangeType
import com.miassolutions.milkledger.core.ui.sort.FilterOptions
import com.miassolutions.milkledger.core.ui.sort.SortOrder
import com.miassolutions.milkledger.data.repositories.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SupplierDetailViewModel @Inject constructor(private val repository: PurchaseRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(SupplierDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun onSelectedSupplierId(id: String) {
        _uiState.update { it.copy(selectedSupplierId = id) }
        loadDetails()
    }

    fun onEvent(event: SupplierUiEvent) {
        when (event) {
            is SupplierUiEvent.ApplyFilter -> applyFilter(event.filter)
            is SupplierUiEvent.ChangeDateRange -> changeDateRange(event.rangeType)
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            val id = _uiState.value.selectedSupplierId ?: return@launch

            repository.getPurchasesForSupplier(id).collect { list ->
                val details = list.map { it.toSupplierDetailModel() }
                _uiState.update {
                    it.copy(
                        supplierDetailList = details,
                        filteredList = details
                    )
                }
                filterData()
            }
        }
    }

    private fun applyFilter(filter: FilterOptions) {
        _uiState.update { it.copy(currentFilter = filter) }
        filterData()
    }

    private fun filterData() {
        val state = _uiState.value
        var filteredList = state.supplierDetailList
        if (filteredList.isEmpty()) return

        viewModelScope.launch {
            val (startDate, endDate) = when {
                state.selectedStartDate != null && state.selectedEndDate != null
                    -> state.selectedStartDate to state.selectedEndDate

                else -> DateRangeHelper.getRange(state.dateRangeType)
            }

            // Apply Date filer
            filteredList = filteredList.filter { detail ->
                detail.date in startDate!!..endDate!!
            }
            // apply sorting
            state.currentFilter.category?.let { category ->
                filteredList = when (category) {
                    "Milk" -> filteredList.sortedBy { it.milkAmount }
                    "Price" -> filteredList.sortedBy { it.milkPrice }
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




    fun changeDateRange(rangeType: DateRangeType) {
        _uiState.update { it.copy(dateRangeType = rangeType) }
        filterData()
    }

    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update {
            it.copy(
                dateRangeType = DateRangeType.CUSTOM,
                selectedStartDate = start,
                selectedEndDate = end
            )
        }
        filterData()
    }



}