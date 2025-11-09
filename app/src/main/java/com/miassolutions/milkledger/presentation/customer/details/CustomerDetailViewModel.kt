package com.miassolutions.milkledger.presentation.customer.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.toRoundedStr

import com.miassolutions.milkledger.data.repository.SalesRepository
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

                val initialDetail = list.map { it.toCustomerDetail() }

                val finalDetail = initialDetail.flagPriceChangeStarts()

                _uiState.update {
                    it.copy(
                        customerDetailList = finalDetail,
                        filteredList = finalDetail
                    )
                }
                filterData() // immediately filter after loading
            }
        }
    }





    private fun filterData() {
        val state = _uiState.value
        val filteredList = state.customerDetailList

        if (filteredList.isEmpty()) return

        viewModelScope.launch {

            val filterStartDate = state.selectedStartDate
            val filterEndDate = state.selectedEndDate

            var currentFilteredList = filteredList

            //core logic

            if (filterStartDate != null && filterEndDate != null) {

                currentFilteredList = filteredList.filter { detail ->
                    detail.date in filterStartDate..filterEndDate
                }
            } else {
                // all data
            }

            val totalMilkAmount = currentFilteredList.sumOf { it.milkAmount }
            val totalDeduction = currentFilteredList.sumOf { it.deduction }
            val totalPrice = currentFilteredList.sumOf { it.milkPrice }
            val totalPaid = currentFilteredList.sumOf { it.payment }
            val balance = currentFilteredList.sumOf { it.balance }

            val customerSummary = CustomerSummary(
                summaryPeriod = "",
                totalMilk = totalMilkAmount.toRoundedStr(),
                totalDeduction = totalDeduction.toRoundedStr(),
                totalPrice = totalPrice.toRoundedStr(),
                paidAmount = totalPaid.toRoundedStr(),
                balance = balance
            )

            _uiState.update {
                it.copy(
                    filteredList = currentFilteredList,
                    summary = customerSummary
                )
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

}
