package com.miassolutions.milkledger.presentation.customer.sales.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SaleAddViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _isDuplicate = MutableLiveData<Boolean>()
    val isDuplicate: LiveData<Boolean> get() = _isDuplicate

    val customers : LiveData<List<CustomerEntity>> = repository.observeCustomersList().asLiveData()

    fun isDuplicateSale(customerId: String?, date: LocalDate?) {
        viewModelScope.launch {
            val result =
                repository.isDuplicateSale(customerId ?: "", date = date ?: LocalDate.now())
            _isDuplicate.value = result
        }
    }

    fun checkDuplicate(
        customerId: String?,
        date: LocalDate?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (customerId == null || date == null) {
                onResult(false)
                return@launch
            }
            val result = repository.isDuplicateSale(customerId, date)
            onResult(result)
        }
    }





    fun addSale(sale: SalesEntity) {
        viewModelScope.launch {
            repository.insertSale(sale)
        }
    }
}