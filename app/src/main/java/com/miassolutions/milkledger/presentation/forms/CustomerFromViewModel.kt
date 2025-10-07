package com.miassolutions.milkledger.presentation.forms


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.domain.model.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerFormViewModel @Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _customerList = MutableStateFlow<List<Customer>>(emptyList())
    val customerList = _customerList.asStateFlow()

    private val _saveStatus = MutableStateFlow<Boolean?>(null)
    val saveStatus = _saveStatus.asStateFlow()

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                val customerEntity = customer.toEntity()
                repository.insertCustomer(customerEntity)
                _saveStatus.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _saveStatus.value = false
            }
        }
    }

    fun resetStatus() {
        _saveStatus.value = null
    }


}
