package com.miassolutions.milkledger.presentation.supplier.form



import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.repository.SupplierRepository
import com.miassolutions.milkledger.domain.model.Supplier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SupplierFormViewModel @Inject constructor(
    private val repository: SupplierRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editingSupplier: Supplier? = savedStateHandle["supplier"]

    private val _uiState = MutableStateFlow(
        SupplierFormUiState(
            name = editingSupplier?.name.orEmpty(),
            rate = editingSupplier?.rate?.toString().orEmpty(),
            position = editingSupplier?.sortOrder?.toString().orEmpty(),
            advanceAmount = editingSupplier?.advanceAmount?.toString().orEmpty(),
            isEdit = editingSupplier != null
        )
    )
    val uiState: StateFlow<SupplierFormUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<SupplierFormUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // INPUTS
    fun onNameChanged(value: String) =
        _uiState.update { it.copy(name = value, nameError = null) }

    fun onRateChanged(value: String) =
        _uiState.update { it.copy(rate = value, rateError = null) }

    fun onPositionChanged(value: String) =
        _uiState.update { it.copy(position = value, positionError = null) }

    fun onAdvanceAmountChanged(value: String) =
        _uiState.update { it.copy(advanceAmount = value) }

    // SAVE
    fun onSaveClicked() {
        val state = _uiState.value
        val rate = state.rate.toDoubleOrNull()
        val position = state.position.toIntOrNull()

        var valid = true

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            valid = false
        }

        if (rate == null) {
            _uiState.update { it.copy(rateError = "Invalid rate") }
            valid = false
        }

        if (position == null) {
            _uiState.update { it.copy(positionError = "Invalid position") }
            valid = false
        }

        if (!valid) return

        val supplier = Supplier(
            id = editingSupplier?.id ?: UUID.randomUUID().toString(),
            name = state.name.trim(),
            rate = rate!!,
            sortOrder = position!!,
            advanceAmount = state.advanceAmount.toDoubleOrNull() ?: 0.0,
            isDefault = true
        )

        viewModelScope.launch {
            repository.upsertSupplier(supplier)
            _uiEvent.emit(SupplierFormUiEvent.Dismiss)
        }
    }
}
