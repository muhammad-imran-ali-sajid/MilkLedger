package com.miassolutions.milkledger.features.sale.ui.saleform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.customer.ui.mapper.toDropDownUi
import com.miassolutions.milkledger.features.customer.ui.model.DropDownCustomerListUi
import com.miassolutions.milkledger.features.sale.data.repository.MilkSaleRepository
import com.miassolutions.milkledger.features.sale.domain.model.Sale
import com.miassolutions.milkledger.features.sale.domain.usecase.CalculateSaleUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.CheckDuplicateSaleUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.GetSaleByIdUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.ObserveCustomerUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.SaveSaleUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.UpdateSaleUseCase
import com.miassolutions.milkledger.features.sale.mapper.toDomain
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SaleFormViewModel @Inject constructor(
    private val repository: MilkSaleRepository
) : BaseViewModel<SaleFormUiState, SaleFormUiEvent, SaleFormUiEffect>(SaleFormUiState()) {

    // Customers List alag flow me rakhna behtar hai state se
    val customersList = repository.getCustomers()

    override fun onEvent(event: SaleFormUiEvent) {
        when (event) {
            is SaleFormUiEvent.OnDateSelected -> updateState { it.copy(date = event.date) }

            is SaleFormUiEvent.OnCustomerSelected -> {
                // Customer select hotay hi Rate aur Balance fetch karein
                updateState { it.copy(
                    selectedCustomer = event.customer,
                    rate = event.customer.defaultRate.toString() // Auto-fill Rate
                ) }
                fetchBalance(event.customer.accountId)
                calculateTotal()
            }

            is SaleFormUiEvent.OnVolumeChanged -> {
                updateState { it.copy(volume = event.value) }
                calculateTotal()
            }
            is SaleFormUiEvent.OnDeductionChanged -> {
                updateState { it.copy(deduction = event.value) }
                calculateTotal()
            }
            is SaleFormUiEvent.OnRateChanged -> {
                updateState { it.copy(rate = event.value) }
                calculateTotal()
            }
            is SaleFormUiEvent.OnAmountPaidChanged -> {
                updateState { it.copy(amountPaid = event.value) }
            }
            is SaleFormUiEvent.OnNoteChanged -> updateState { it.copy(note = event.value) }

            is SaleFormUiEvent.OnSaveClicked -> saveSale()

            SaleFormUiEvent.OnDateClick -> emitEffect(SaleFormUiEffect.OpenDatePicker)
        }
    }

    private fun fetchBalance(accountId: String) {
        viewModelScope.launch {
            repository.getCustomerBalance(accountId).collect { balance ->
                updateState { it.copy(currentBalance = balance) }
            }
        }
    }

    private fun calculateTotal() {
        val state = currentState
        val vol = state.volume.toDoubleOrNull() ?: 0.0
        val ded = state.deduction.toDoubleOrNull() ?: 0.0
        val rate = state.rate.toDoubleOrNull() ?: 0.0

        // ✅ Using Your Utils
        val total = MilkCalculationUtils.calculateCustomerPrice(vol, ded, rate)

        updateState { it.copy(calculatedTotal = total) }
    }

    private fun saveSale() {
        val state = currentState
        if (state.selectedCustomer == null) {
            emitEffect(SaleFormUiEffect.ShowSnackbar("Please select a customer"))
            return
        }
        val vol = state.volume.toDoubleOrNull()
        if (vol == null || vol <= 0) {
            emitEffect(SaleFormUiEffect.ShowSnackbar("Please enter valid volume"))
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            try {
                repository.saveMilkSale(
                    dateMillis = state.date.toMillis(),
                    accountId = state.selectedCustomer!!.accountId,
                    volume = vol,
                    deduction = state.deduction.toDoubleOrNull() ?: 0.0,
                    rate = state.rate.toDoubleOrNull() ?: 0.0,
                    amountPaid = (state.amountPaid.toDoubleOrNull() ?: 0.0).toLong() * 100, // Rs to Paisa
                    note = state.note
                )
                emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Saved Successfully"))
                emitEffect(SaleFormUiEffect.NavigateBack)
            } catch (e: Exception) {
                emitEffect(SaleFormUiEffect.ShowSnackbar("Error: ${e.message}"))
                updateState { it.copy(isSaving = false) }
            }
        }
    }
}