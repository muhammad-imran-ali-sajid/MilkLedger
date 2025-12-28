package com.miassolutions.milkledger.presentation.supplier.suppliers

import androidx.lifecycle.ViewModel
import com.miassolutions.milkledger.data.repository.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SupplierListViewModel @Inject constructor(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierListUiState())
    val uiState = _uiState.asStateFlow()

//    private val _uiEvent = MutableSharedFlow<SupplierUiEvent>()
//    val uiEvent = _uiEvent.asSharedFlow()
//
//    init {
//        observeSuppliers()
//    }
//
//    private fun observeSuppliers() {
//        viewModelScope.launch {
//            repository.getAllSuppliers()
//                .collect { suppliers ->
//                    _uiState.value = _uiState.value.copy(
//                        suppliers = suppliers
//                    )
//                }
//        }
//    }
//
//    fun onAddSupplierClick() {
//        viewModelScope.launch {
//            _uiEvent.emit(SupplierUiEvent.ShowSupplierForm)
//        }
//    }
//
//    fun deleteSupplier(supplier: Supplier) {
//        viewModelScope.launch {
//            repository.deleteSupplier(supplier.id)
//            _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier deleted"))
//        }
//    }
}
