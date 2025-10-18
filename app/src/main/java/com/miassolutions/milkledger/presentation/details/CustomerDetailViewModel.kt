package com.miassolutions.milkledger.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miassolutions.milkledger.data.repositories.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(private val repository: SalesRepository) :
    ViewModel() {

    private val _uiState = MutableStateFlow(CustomerDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun onSelectedCustomerId(id: String) {
        _uiState.update { it.copy(selectedCustomerId = id) }
    }

    init {
        loadDetails()
    }


    fun loadDetails() {
        viewModelScope.launch {
            val id = _uiState.value.selectedCustomerId
            id?.let {

                val detailList = repository.getSalesForCustomer(id).first()
                val list = mutableListOf<CustomerDetailModel>()
                detailList.forEach {
                    val customer = it.toCustomerDetail()
                    list.add(customer)
                }
                _uiState.update { it.copy(customerDetailList = list) }

            }

        }
    }


}