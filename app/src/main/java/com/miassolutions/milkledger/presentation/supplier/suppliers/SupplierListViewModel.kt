package com.miassolutions.milkledger.presentation.supplier.suppliers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.SupplierRepository
import com.miassolutions.milkledger.presentation.supplier.mapper.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierListViewModel @Inject constructor(
    private val repository: SupplierRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupplierListUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SupplierListUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()


    fun onEvent(event: SupplierListUiEvent) {
        when (event) {
            is SupplierListUiEvent.OnAddSupplierClick -> {
                emitEffect(SupplierListUiEffect.NavToAddSupplierForm)
            }

            is SupplierListUiEvent.OnSupplierItemClick -> {
                emitEffect(SupplierListUiEffect.OpenOptionDialog(event.supplierId))
            }

            is SupplierListUiEvent.OnDeleteSupplierClick -> {
                deleteSupplier(event.supplierId)
            }

            is SupplierListUiEvent.OnRetryClick -> {}
            is SupplierListUiEvent.OnSearchQueryChange -> {
                updateState { it.copy(searchQuery = event.name) }
            }
        }
    }

    init {
        loadSuppliers()
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliers()
                .onStart {
                    updateState { it.copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            error = e.localizedMessage ?: "Unknown error"
                        )
                    }
                }
                .collect { suppliers ->
                    val suppliersUi = suppliers.map { it.toUi() }
                    updateState { it.copy(suppliers = suppliersUi, isLoading = false) }
                }
        }
    }


    private fun emitEffect(effect: SupplierListUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }

    private fun deleteSupplier(supplierId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSupplier(supplierId)
                emitEffect(SupplierListUiEffect.ShowMessage("Supplier deleted"))
            } catch (e: Exception) {
                emitEffect(SupplierListUiEffect.ShowMessage("Failed to delete: ${e.localizedMessage}"))
            }
        }
    }


    private fun updateState(reducer: (SupplierListUiState) -> SupplierListUiState) {
        _uiState.update(reducer)
    }


}
