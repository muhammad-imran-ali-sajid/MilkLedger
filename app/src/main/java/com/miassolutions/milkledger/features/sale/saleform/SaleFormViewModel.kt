package com.miassolutions.milkledger.features.sale.saleform

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import com.miassolutions.milkledger.features.sale.model.UpdateSaleRequest
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SaleFormViewModel @Inject constructor(
    private val repository: MilkSaleRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<SaleFormUiState, SaleFormUiEvent, SaleFormUiEffect>(SaleFormUiState()) {

    private val saleId: String? = savedStateHandle["saleId"]
    val passedDate = savedStateHandle["saleDate"] ?: -1L

    val customersList = repository.getCustomers()

    init {
        if (saleId != null) {
            loadSaleForEdit(saleId)
        } else {
            val initialDate = if (passedDate != -1L) passedDate.toLocalDate() else LocalDate.now()
            updateState { it.copy(date = initialDate, paymentDate = initialDate) }
        }
    }

    private fun loadSaleForEdit(id: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            // Repository se Data mangwayen (Jo hum ne DAO me update kia tha)
            val sale = repository.getSaleById(id)

            if (sale != null) {
                // Customer dhoondnay k liye
                val allCustomers = repository.getCustomers().firstOrNull() ?: emptyList()
                val customer = allCustomers.find { it.accountId == sale.customerId }

                updateState {
                    it.copy(
                        isLoading = false,

                        // ✅ Flag set karein taake Fragment Customer Name ko disable kr sakay
                        isEditMode = true,

                        selectedCustomer = customer,
                        date = sale.dateMillis.toLocalDate(),

                        volume = sale.quantity.toString(),
                        deduction = sale.deduction.toString(),

                        // 🔥 FIX 1: Direct Rate from DB (No Calculation = No Crash)
                        // Pehle hum yahan calculate kr rahy thy jo divide by zero de rha tha
                        rate = sale.rate.toString(),

                        amountPaid = (sale.paymentReceived / 100.0).toString(),
                        note = sale.note ?: "",

                        // Total
                        calculatedTotal = (sale.totalAmount / 100.0)
                    )
                }

                // Balance bhi fetch kar len
                if (customer != null) fetchBalance(customer.accountId)

            } else {
                emitEffect(SaleFormUiEffect.ShowSnackbar("Sale not found"))
                emitEffect(SaleFormUiEffect.NavigateBack)
            }
        }
    }

    override fun onEvent(event: SaleFormUiEvent) {
        when (event) {
            is SaleFormUiEvent.OnDateSelected -> updateState { it.copy(date = event.date) }

            is SaleFormUiEvent.OnCustomerSelected -> {
                updateState {
                    it.copy(
                        selectedCustomer = event.customer,
                        rate = event.customer.defaultRate.toString()
                    )
                }
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

            SaleFormUiEvent.OnPaymentDateClick -> emitEffect(SaleFormUiEffect.OpenPaymentDatePicker)

            is SaleFormUiEvent.OnPaymentDateSelected -> {
                updateState { it.copy(paymentDate = event.paymentDate) }
            }

            is SaleFormUiEvent.LoadSaleForEdit -> {
                loadSaleForEdit(event.saleId)
            }
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

        // Utils use kar rahe hain (ye safe hona chahiye)
        val total = MilkCalculationUtils.calculateCustomerPrice(vol, ded, rate)

        updateState { it.copy(calculatedTotal = total) }
    }

    private fun saveSale() {
        val state = currentState

        // 1. Basic Validation
        if (state.selectedCustomer == null) {
            emitEffect(SaleFormUiEffect.ShowSnackbar("Please select a customer"))
            return
        }

        val vol = state.volume.toDoubleOrNull() ?: 0.0
        val payment = state.amountPaid.toDoubleOrNull() ?: 0.0

        if (vol <= 0 && payment <= 0) {
            emitEffect(SaleFormUiEffect.ShowSnackbar("Please enter volume or payment"))
            return
        }

        // 🔥 FIX 2: Safety Check for NaN/Infinity
        // Agar user ne Rate field me kuch garbar ki ya copy paste se NaN aya
        val rawRate = state.rate.toDoubleOrNull() ?: 0.0
        val finalRate = if (rawRate.isNaN() || rawRate.isInfinite()) 0.0 else rawRate

        // Deduction check (Optional but recommended)
        val ded = state.deduction.toDoubleOrNull() ?: 0.0
        if(ded > vol) {
            emitEffect(SaleFormUiEffect.ShowSnackbar("Deduction cannot be greater than Volume"))
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            try {

                if (saleId != null) {
                    // --- UPDATE LOGIC ---
                    val updateRequest = UpdateSaleRequest(
                        saleId = saleId,
                        accountId = state.selectedCustomer!!.accountId,
                        date = state.date,
                        volume = vol,
                        deduction = ded,

                        // ✅ Use Safe Final Rate
                        rate = finalRate,

                        amountPaid = (payment * 100).toLong(),
                        note = state.note
                    )

                    repository.updateMilkSale(updateRequest)
                    emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Updated Successfully"))

                } else {
                    // --- NEW SAVE LOGIC ---
                    repository.saveMilkSale(
                        accountId = state.selectedCustomer!!.accountId,
                        volume = vol,
                        deduction = ded,

                        // ✅ Use Safe Final Rate
                        rate = finalRate,

                        amountPaid = (payment * 100).toLong(),
                        note = state.note,
                        saleDate = state.date,
                        paymentDate = state.paymentDate
                    )
                    emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Saved Successfully"))
                }

                emitEffect(SaleFormUiEffect.NavigateBack)

            } catch (e: Exception) {
                Log.e("SaleFormViewModel", "Error saving sale", e)
                emitEffect(SaleFormUiEffect.ShowSnackbar("Error: ${e.message}"))
            } finally {
                updateState { it.copy(isSaving = false) }
            }
        }
    }
}