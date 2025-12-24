package com.miassolutions.milkledger.presentation.supplier.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.oldmapper.toDomain
import com.miassolutions.milkledger.data.oldmapper.toEntity
import com.miassolutions.milkledger.data.repository.SupplierRepository
import com.miassolutions.milkledger.domain.model.Supplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierListViewModel @Inject constructor(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SupplierUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        observeSuppliers()
    }

    private var allSuppliers: List<Supplier> = emptyList()

    private fun observeSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliers()
                .map { list -> list.map { it.toDomain() } } // Convert to domain model
                .collect { suppliers ->
                    allSuppliers = suppliers
                    _uiState.value =
                        _uiState.value.copy(
                            suppliers = suppliers,
                            displayedSuppliers = suppliers
                        )
                }
        }
    }



    /** ➕ Add or edit supplier */
    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                val existing = supplier.id?.let { repository.getSupplierById(it) }

                if (existing != null) {
                    repository.updateSupplier(supplier.toEntity())
                    _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier updated"))
                } else {
                    // determine next sort order
                    val nextSortOrder = (allSuppliers.maxOfOrNull { it.sortOrder } ?: 0) + 1
                    repository.insertSupplier(supplier.toEntity().copy(sortOrder = nextSortOrder))
                    _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier added"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Error saving supplier"))
            }
        }
    }

    /** 🔁 Save the new supplier order after drag & drop */
    fun saveNewOrder(reorderedSuppliers: List<Supplier>) {
        viewModelScope.launch {
            reorderedSuppliers.forEachIndexed { index, supplier ->
                repository.updateSupplier(
                    supplier.toEntity().copy(sortOrder = index)
                )
            }
        }
    }


//    fun saveNewOrder(reorderedSuppliers: List<SupplierEntity>) {
//        viewModelScope.launch {
//            reorderedSuppliers.forEachIndexed { index, supplier ->
//                repository.updateSupplier(supplier.copy(sortOrder = index))
//            }
//        }
//    }



    fun onAddSupplierClick() {
        viewModelScope.launch {
            _uiEvent.emit(SupplierUiEvent.ShowSupplierForm)
        }
    }

//    fun saveSupplier(supplier: Supplier) {
//        viewModelScope.launch {
//            try {
//                val existing = supplier.id?.let { repository.getSupplierById(it) }
//
//                if (existing != null) {
//                    repository.updateSupplier(supplier.toEntity())
//                    _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier updated"))
//                } else {
//                    repository.insertSupplier(supplier.toEntity())
//                    _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier saved"))
//                }
//            } catch (e: Exception) {
//                _uiEvent.emit(SupplierUiEvent.ShowMessage("Error saving supplier"))
//            }
//        }
//    }



    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(supplier.toEntity())
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier deleted"))
            } catch (e: Exception) {
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Error deleting supplier"))
            }
        }
    }


}
