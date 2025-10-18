package com.miassolutions.milkledger.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.sort.FilterOptions
import com.miassolutions.milkledger.core.ui.sort.SortOrder
import com.miassolutions.milkledger.data.repositories.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CustomerUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onEvent(event: CustomerUiEvent) {
        when (event) {
            is CustomerUiEvent.ApplyFilter -> {
                applyFilter(event.filter)
            }
        }
    }

    fun onSelectedCustomerId(id: String, name: String) {
        _uiState.update { it.copy(selectedCustomerId = id, customerName = name) }
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            val id = _uiState.value.selectedCustomerId ?: return@launch
            val details = repository.getSalesForCustomer(id).first().map { it.toCustomerDetail() }

            _uiState.update { it.copy(customerDetailList = details, filteredList = details) }
        }
    }

    private fun applyFilter(filter: FilterOptions) {
        viewModelScope.launch {
            val originalList =
                repository.getSalesForCustomer(_uiState.value.selectedCustomerId!!).first()
                    .map { it.toCustomerDetail() }

            var filteredList = originalList

            // Apply category (column) sorting
            filter.category?.let { category ->
                filteredList = when (category) {
                    "Date" -> filteredList.sortedBy { it.date }
                    "Net Milk" -> filteredList.sortedBy { it.netMilk }
                    "Price" -> filteredList.sortedBy { it.milkPrice }
                    "Balance" -> filteredList.sortedBy { it.balance }
                    "Payment" -> filteredList.sortedBy { it.payment }
                    else -> filteredList
                }
            }

            // Apply A-Z or Z-A order
            filteredList = when (filter.sortOrder) {
                SortOrder.ASCENDING -> filteredList
                SortOrder.DESCENDING -> filteredList.reversed()
                SortOrder.NONE -> filteredList
            }

            _uiState.update { it.copy(customerDetailList = filteredList) }
        }

        fun clearFilter() {
            val list = _uiState.value.customerDetailList
            _uiState.update { it.copy(filteredList = list, currentFilter = FilterOptions()) }
        }
    }
}
