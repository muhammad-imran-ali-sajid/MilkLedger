package com.miassolutions.milkledger.presentation.supplier.purchase

import androidx.lifecycle.LiveData
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
import javax.inject.Inject

@HiltViewModel
class PurchaseAddViewModel @Inject constructor(
    private val repository: PurchaseRepository
) : ViewModel() {

    val suppliers: LiveData<List<SupplierEntity>> = repository.observeSuppliersList().asLiveData()

    fun addPurchase(purchase: PurchaseEntity) {
        viewModelScope.launch {
            repository.insertPurchase(purchase)
        }
    }


}