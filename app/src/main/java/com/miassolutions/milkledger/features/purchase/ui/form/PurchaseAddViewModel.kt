package com.miassolutions.milkledger.features.purchase.ui.form

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.purchase.data.PurchaseEntity
import com.miassolutions.milkledger.features.purchase.data.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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