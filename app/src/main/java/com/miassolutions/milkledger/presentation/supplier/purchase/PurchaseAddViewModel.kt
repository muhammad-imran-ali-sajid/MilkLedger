package com.miassolutions.milkledger.presentation.supplier.purchase

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.data.repository.SupplierRepository
import com.miassolutions.milkledger.domain.model.Supplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PurchaseAddViewModel @Inject constructor(
    private val repository: PurchaseRepository
) : ViewModel() {

    private val _isDuplicate = MutableLiveData<Boolean>()
    val isDuplicate: LiveData<Boolean> get() = _isDuplicate


//    val suppliers: LiveData<List<SupplierEntity>> = repository.observeSuppliersList().asLiveData()

    fun isDuplicatePurchase(supplierId: String?, date: LocalDate?) {
        viewModelScope.launch {
            val result =
                repository.isDuplicatePurchase(supplierId ?: "", date = date ?: LocalDate.now())
            _isDuplicate.value = result
        }
    }

    fun checkDuplicate(
        supplierId: String?,
        date: LocalDate?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (supplierId == null || date == null) {
                onResult(false)
                return@launch
            }
            val result = repository.isDuplicatePurchase(supplierId, date)
            onResult(result)
        }
    }



    fun addPurchase(purchase: PurchaseEntity) {
//        viewModelScope.launch {
//            repository.insertPurchase(purchase)
//        }
    }


}