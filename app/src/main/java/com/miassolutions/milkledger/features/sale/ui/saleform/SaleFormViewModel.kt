package com.miassolutions.milkledger.features.sale.ui.saleform

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.customer.ui.mapper.toDropDownUi
import com.miassolutions.milkledger.features.customer.ui.model.DropDownCustomerListUi
import com.miassolutions.milkledger.features.milk.MilkSaleRepository
import com.miassolutions.milkledger.features.milk.model.UpdateSaleRequest
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
import com.miassolutions.milkledger.utils.extensions.toPrice
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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

        Log.d("SaleFormViewModel", saleId?:"no id")

        if (saleId != null){
            loadSaleForEdit(saleId)
        } else {

            val initialDate = if (passedDate != -1L) passedDate.toLocalDate() else LocalDate.now()

            updateState { it.copy(date = initialDate, paymentDate = initialDate) }
        }



    }

    private fun loadSaleForEdit(id: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            // Repository se Data mangwayen
            val sale = repository.getSaleById(id)

            if (sale != null) {
                // Customer dhoondnay k liye (List se match karein)
                val allCustomers = repository.getCustomers().firstOrNull() ?: emptyList()
                val customer = allCustomers.find { it.accountId == sale.customerId }

                updateState {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,

                        // Fields set karein
                        selectedCustomer = customer,
                        date = sale.dateMillis.toLocalDate(),

                        // Numeric Values (String me convert kr k)
                        volume = sale.quantity.toString(), // Ya netQuantity
                        deduction = sale.deduction.toString(),
                        rate = (sale.totalAmount / sale.netQuantity / 100.0).toString(), // Reverse calculate rate if needed or use saved rate

                        // Amount Paid (Paisa -> Rupees)
                        amountPaid = (sale.paymentReceived / 100.0).toString(),

                        note = sale.note ?: "",

                        // Initial Total
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
                // Customer select hotay hi Rate aur Balance fetch karein
                updateState {
                    it.copy(
                        selectedCustomer = event.customer,
                        rate = event.customer.defaultRate.toString() // Auto-fill Rate
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

        // ✅ Using Your Utils
        val total = MilkCalculationUtils.calculateCustomerPrice(vol, ded, rate)

        updateState { it.copy(calculatedTotal = total) }
    }

    private fun saveSale() {
        val state = currentState

        // Validations (Same as before)
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

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            try {

                // 🔥 CHECK: Kya ye Edit hai ya New?
                if (saleId != null) {
                    // --- UPDATE LOGIC ---
                    val updateRequest = UpdateSaleRequest(
                        saleId = saleId,
                        accountId = state.selectedCustomer!!.accountId,
                        date = state.date,
                        volume = vol,
                        deduction = state.deduction.toDoubleOrNull() ?: 0.0,
                        rate = state.rate.toDoubleOrNull() ?: 0.0,
                        amountPaid = (payment * 100).toLong(), // Rs -> Paisa
                        note = state.note
                    )

                    repository.updateMilkSale(updateRequest)
                    emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Updated Successfully"))

                } else {
                    // --- NEW SAVE LOGIC --- (Old code)
                    repository.saveMilkSale(
                        accountId = state.selectedCustomer!!.accountId,
                        volume = vol,
                        deduction = state.deduction.toDoubleOrNull() ?: 0.0,
                        rate = state.rate.toDoubleOrNull() ?: 0.0,
                        amountPaid = (payment * 100).toLong(),
                        note = state.note,
                        saleDate = state.date,
                        paymentDate = state.paymentDate
                    )
                    emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Saved Successfully"))
                }

                emitEffect(SaleFormUiEffect.NavigateBack)

            } catch (e: Exception) {
                Log.d("SaleFormViewModel", e.localizedMessage ?: e.message.toString())
                emitEffect(SaleFormUiEffect.ShowSnackbar("Error: ${e.message}"))
            } finally {
                updateState { it.copy(isSaving = false) }
            }
        }
    }
}