package com.miassolutions.milkledger.features.supplier.ui.form


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.features.supplier.data.repository.SupplierRepository
import com.miassolutions.milkledger.features.supplier.data.repository.SupplierSaveError
import com.miassolutions.milkledger.features.supplier.domain.Supplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SupplierFormViewModel @Inject constructor(
    private val repository: SupplierRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val supplierId: String? = savedStateHandle["supplierId"]

    private val _uiState = MutableStateFlow(SupplierFormUiState())
    val uiState = _uiState.asStateFlow()

    init {
        supplierId?.let { loadSupplier(it) }

    }

    private fun loadSupplier(supplierId: String) {
        viewModelScope.launch {
            repository.getSupplierById(supplierId)
                .filterNotNull()
                .first()
                .let { supplier ->
                    updateState {
                        it.copy(
                            name = supplier.name,
                            position = supplier.sortOrder.toString(),
                            rate = supplier.rate.toString(),
                            advanceAmount = supplier.advanceAmount.toString(),
                            isEdit = true

                        )
                    }

                }
        }
    }


    private val _uiEffect = MutableSharedFlow<SupplierFormUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onEvent(event: SupplierFormUiEvent) {
        when (event) {
            is SupplierFormUiEvent.OnPositionChanged -> {
                updateState { it.copy(position = event.position, positionError = null) }
            }

            is SupplierFormUiEvent.OnNameChanged -> {
                updateState { it.copy(name = event.name, nameError = null) }
            }

            is SupplierFormUiEvent.OnRateChanged -> {
                updateState { it.copy(rate = event.rate, rateError = null) }
            }

            is SupplierFormUiEvent.OnAdvanceAmountChanged -> {
                updateState { it.copy(advanceAmount = event.amount) }
            }

            SupplierFormUiEvent.OnSaveClicked -> {
                onSaveSupplier()
            }

        }
    }

    private fun onSaveSupplier() {
        val state = _uiState.value

        val position = state.position.toIntOrNull()
        val name = state.name.trim()
        val rate = state.rate.toDoubleOrNull()

        val positionError = if (position == null) "Position is required" else null
        val nameError = if (name.isBlank()) "Name is required" else null
        val rateError = if (rate == null) "Rate is required" else null

        val hasError = positionError != null || nameError != null || rateError != null

        if (hasError) {
            updateState {
                it.copy(
                    isSaving = false,
                    nameError = nameError,
                    rateError = rateError,
                    positionError = positionError
                )
            }
            return
        }

        val supplier = Supplier(
            id = supplierId ?: UUID.randomUUID().toString(),
            name = name,
            rate = rate ?: 0.0,
            sortOrder = position ?: 0,
            advanceAmount = state.advanceAmount.toDoubleOrNull() ?: 0.0,
            isDefault = true
        )

        viewModelScope.launch {
            updateState {
                it.copy(
                    isSaving = true,
                    nameError = null,
                    positionError = null,
                    rateError = null
                )
            }

            try {
                repository.upsertSupplier(supplier)
                emitEffect(SupplierFormUiEffect.Dismiss)
            } catch (e: SupplierSaveError.SortOrderAlreadyExists) {
                updateState {
                    it.copy(
                        isSaving = false,
                        positionError = "Position ${e.sortOrder} already exists"
                    )
                }
            } catch (e: Exception) {
                updateState { it.copy(isSaving = false) }
            }
        }


    }


    private fun emitEffect(effect: SupplierFormUiEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }

    private fun updateState(reducer: (SupplierFormUiState) -> SupplierFormUiState) {
        _uiState.update(reducer)
    }
}
