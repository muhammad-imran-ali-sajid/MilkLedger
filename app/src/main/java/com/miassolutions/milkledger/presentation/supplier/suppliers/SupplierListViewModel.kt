package com.miassolutions.milkledger.presentation.supplier.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.mapper.toDomain
import com.miassolutions.milkledger.data.mapper.toEntity
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.repository.SupplierRepository
import com.miassolutions.milkledger.domain.model.Customer
import com.miassolutions.milkledger.domain.model.Supplier
import com.miassolutions.milkledger.presentation.customer.customers.CustomerUiEvent
import com.miassolutions.milkledger.presentation.customer.customers.CustomerUiState
import com.miassolutions.sort_filter.FilterOption
import com.miassolutions.sort_filter.SortOption
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

    /**  Called from Fragment after user applies sort/filter */

    fun applySortAndFilter(filters: List<FilterOption>, sorts: List<SortOption>) {
        viewModelScope.launch {
            var filtered = allSuppliers

            // 🔹 Apply filters
            if (filters.any { it.isSelected }) {
                val selectedIds = filters.filter { it.isSelected }.map { it.id }
                filtered = filtered.filter { supplier ->
                    when {
                        "active" in selectedIds -> supplier.rate >= 0.0
                        "inactive" in selectedIds -> supplier.rate == 0.0
                        else -> true
                    }
                }
            }

            // 🔹 Apply sorting
            val selectedSort = sorts.find { it.isSelected }
            val sorted = when (selectedSort?.id) {
                "name" -> if (selectedSort.ascending)
                    filtered.sortedBy { it.name.lowercase() }
                else filtered.sortedByDescending { it.name.lowercase() }

                "date" -> if (selectedSort.ascending)
                    filtered.sortedBy { it.name }
                else filtered.sortedByDescending { it.name }

                else -> filtered
            }

            _uiState.value = _uiState.value.copy(displayedSuppliers = sorted)
        }
    }


    fun onAddSupplierClick() {
        viewModelScope.launch {
            _uiEvent.emit(SupplierUiEvent.ShowSupplierForm)
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                val existing = supplier.id?.let { repository.getSupplierById(it) }

                if (existing != null) {
                    repository.updateSupplier(supplier.toEntity())
                    _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier updated"))
                } else {
                    repository.insertSupplier(supplier.toEntity())
                    _uiEvent.emit(SupplierUiEvent.ShowMessage("Supplier saved"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(SupplierUiEvent.ShowMessage("Error saving supplier"))
            }
        }
    }



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
