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
        val filteredList = state.supplierDetailList

        if (filteredList.isEmpty()) return

        viewModelScope.launch {

            // Use the dates from the state. They will be null for "All Data".
            val filterStartDate = state.selectedStartDate
            val filterEndDate = state.selectedEndDate

            // Initialize the list to the full list (no filtering)
            var currentFilteredList = filteredList

            // --- Core Logic Improvement ---
            if (filterStartDate != null && filterEndDate != null) {
                // ONLY apply Date filter if BOTH dates are explicitly set (non-null)
                currentFilteredList = filteredList.filter { detail ->
                    // The filter range is applied to the full list
                    detail.date in filterStartDate..filterEndDate
                }
                Log.d("SupplierDetailViewModel", "Filter dates: $filterStartDate - $filterEndDate")
            } else {
                // If one or both dates are null, we show "All Data".
                // currentFilteredList remains the full list.
                Log.d("SupplierDetailViewModel", "Showing All Data (No date filter applied)")
            }


            // --- Summary Calculation (Uses the potentially filtered list) ---
            val avgTS = if (currentFilteredList.isNotEmpty())
                currentFilteredList.sumOf { it.ts } / currentFilteredList.size
            else 0.0
            val totalMilk = currentFilteredList.sumOf { it.milkAmount }
            val totalPrice = currentFilteredList.sumOf { it.milkPrice }
            val totalPaid = currentFilteredList.sumOf { it.payment }
            val balance = currentFilteredList.sumOf { it.balance }

            val supplierSummary = SupplierSummary(
                summaryPeriod = "", // UI text handled separately below
                totalMilk = totalMilk.toRoundedStr(),
                avgTS = avgTS.toRoundedStr("%.2f"),
                totalPrice = totalPrice.toRoundedStr(),
                paidAmount = totalPaid.toRoundedStr(),
                balance = balance
            )

            Log.d("SupplierDetailViewModel", supplierSummary.toString())


            // Update the state
            _uiState.update {
                it.copy(
                    filteredList = currentFilteredList,
                    summary = supplierSummary,
                    // selectedStartDate and selectedEndDate are already correct in the state,
                    // as they were set via `setCustomDateRange` or cleared to null to show "All Data".
                )
            }
        }
    }


    fun setCustomDateRange(start: LocalDate, end: LocalDate) {
        _uiState.update {
            it.copy(selectedStartDate = start, selectedEndDate = end)
        }
        filterData()
    }


}