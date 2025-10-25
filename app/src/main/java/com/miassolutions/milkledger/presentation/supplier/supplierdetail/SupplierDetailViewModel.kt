package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.toRoundedStr
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
            is SupplierUiEvent.ChangeDateRange -> filterData()
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            val id = _uiState.value.selectedSupplierId ?: return@launch

            repository.getPurchasesForSupplier(id).collect { list ->
                // 1. Convert raw data to detail models
                val initialDetails = list.map { it.toSupplierDetailModel() }

                // 2. Apply the consecutive change logic
                val finalDetails = initialDetails.flagRateChangeStarts()

                _uiState.update {
                    it.copy(
                        supplierDetailList = finalDetails,
                        filteredList = finalDetails,


                        )
                }

                filterData()
            }
        }
    }


    // ... inside SupplierDetailViewModel.kt

    private fun filterData() {
        val state = _uiState.value
        var filteredList = state.supplierDetailList

        if (filteredList.isEmpty()) return

        viewModelScope.launch {
            // Determine the dates to use for filtering
            val (filterStartDate, filterEndDate) = when {
                // Case 1: Dates were explicitly set (via setCustomDateRange)
                state.selectedStartDate != null && state.selectedEndDate != null
                    -> {
                    // Use the selected dates
                    state.selectedStartDate to state.selectedEndDate
                }

                // Case 2: Initial load (both are null) or dates were reset (if you add a reset feature)
                else -> {
                    // Use the default date range
                    LocalDate.now().minusYears(1) to LocalDate.now()
                }
            }


            // Apply Date filer
            filteredList = filteredList.filter { detail ->
                // Use the determined non-null dates
                detail.date in filterStartDate..filterEndDate
            }

            // ... (Summary calculation logic remains the same) ...
            val avgTS = filteredList.sumOf { it.ts } / filteredList.size
            val totalMilk = filteredList.sumOf { it.milkAmount }
            val totalPrice = filteredList.sumOf { it.milkPrice }
            val totalPaid = filteredList.sumOf { it.payment }
            val balance = filteredList.sumOf { it.balance }

            val supplierSummary = SupplierSummary(
                summaryPeriod = "",
                totalMilk = totalMilk.toRoundedStr(),
                avgTS = avgTS.toRoundedStr("%.2f"),
                totalPrice = totalPrice.toRoundedStr(),
                paidAmount = totalPaid.toRoundedStr(),
                balance = balance
            )

            Log.d("SupplierDetailViewModel", "Filter dates: $filterStartDate - $filterEndDate")
            Log.d("SupplierDetailViewModel", supplierSummary.toString())


            // CRITICAL FIX: Update the state with the dates that were actually used for filtering.
            _uiState.update {
                it.copy(
                    filteredList = filteredList,
                    summary = supplierSummary,
                    selectedStartDate = filterStartDate, // <--- ADDED THIS
                    selectedEndDate = filterEndDate      // <--- ADDED THIS
                )
            }
        }
    }


//    fun changeDateRange(rangeType: DateRangeType) {
//        _uiState.update { it.copy(dateRangeType = rangeType) }
//        filterData()
//    }

    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update {
            it.copy(selectedStartDate = start, selectedEndDate = end)
        }
        filterData()
    }


}