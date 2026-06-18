package com.miassolutions.milkledger.features.sale.saleform

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import com.miassolutions.milkledger.features.sale.domain.usecase.DeleteSaleUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.SaveSaleUseCase
import com.miassolutions.milkledger.features.sale.domain.usecase.UpdateSaleUseCase
import com.miassolutions.milkledger.features.sale.model.UpdateSaleRequest
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.LoadSaleForEdit
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnAmountPaidChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnCustomerSelected
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDateClick
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDateSelected
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDeductionChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnDeleteClicked
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnNoteChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnRateChanged
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnSaveAndNewClicked
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnSaveClicked
import com.miassolutions.milkledger.features.sale.saleform.SaleFormUiEvent.OnVolumeChanged
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SaleFormViewModel @Inject constructor(
    private val repository: MilkSaleRepository,
    private val saveSaleUseCase: SaveSaleUseCase,
    private val updateSaleUseCase: UpdateSaleUseCase,
    private val deleteSaleUseCase: DeleteSaleUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<SaleFormUiState, SaleFormUiEvent, SaleFormUiEffect>(SaleFormUiState()) {

    private var balanceJob: Job? = null
    private val saleId: String? = savedStateHandle["saleId"]
    val passedDate = savedStateHandle["saleDate"] ?: -1L

    private val _customersDropDown = MutableStateFlow<List<CustomerDropDownUiModel>>(emptyList())
    val customersDropDown = _customersDropDown.asStateFlow()

    init {
        monitorCustomersStatus()

        if (saleId != null) {
            loadSaleForEdit(saleId)
        } else {
            val initialDate = if (passedDate != -1L) passedDate.toLocalDate() else LocalDate.now()

            // 🔥 NEW ENTRY: Payment Date NULL (User must select)
            updateState {
                it.copy(
                    date = initialDate,
                    paymentDate = initialDate
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun monitorCustomersStatus() {
        viewModelScope.launch {
            combine(
                repository.getCustomers(),
                uiState.map { it.date }.distinctUntilChanged().flatMapLatest { date ->
                    repository.getSalesByDate(date.toMillis())
                }
            ) { customers, salesOnDate ->
                val customersWithEntry = salesOnDate.map { it.customerId }.toSet()
                customers.map { customer ->
                    CustomerDropDownUiModel(
                        account = customer,
                        isEntryDoneToday = customersWithEntry.contains(customer.accountId)
                    )
                }
            }.collect { mappedList ->
                _customersDropDown.value = mappedList
            }
        }
    }

    private fun loadSaleForEdit(id: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            val sale = repository.getSaleById(id)

            if (sale != null) {
                val allCustomers = repository.getCustomers().firstOrNull() ?: emptyList()
                val customer = allCustomers.find { it.accountId == sale.customerId }

                // 🔥 REPOSITORY ALIGNMENT:
                // Ab hum DB se 'paymentDateMillis' direct utha rahay hain.
                val savedPaymentDate = sale.dateMillis.toLocalDate()

                updateState {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,
                        selectedCustomer = customer,
                        date = sale.dateMillis.toLocalDate(),

                        // ✅ DB se Load ki hui date set karen
                        paymentDate = savedPaymentDate,

                        volume = sale.quantity.toString(),
                        deduction = sale.deduction.toString(),
                        rate = sale.rate.toString(),
                        amountPaid = (sale.paymentReceived / 100.0).toString(),
                        note = sale.note ?: "",
                        calculatedTotal = (sale.totalAmount / 100.0)
                    )
                }

                if (customer != null) fetchBalance(customer.accountId)

            } else {
                emitEffect(SaleFormUiEffect.ShowSnackbar("Sale not found"))
                emitEffect(SaleFormUiEffect.NavigateBack)
            }
        }
    }

    override fun onEvent(event: SaleFormUiEvent) {
        when (event) {
            is OnDateSelected -> updateState { it.copy(date = event.date) }
//            is OnPaymentDateSelected -> updateState { it.copy(paymentDate = event.paymentDate) }

            is OnCustomerSelected -> {
                updateState {
                    it.copy(
                        selectedCustomer = event.customer,
                        rate = event.customer.defaultRate.toString()
                    )
                }
                fetchBalance(event.customer.accountId)
                calculateTotal()
            }

            is OnVolumeChanged -> {
                updateState { it.copy(volume = event.value) }
                calculateTotal()
            }

            is OnDeductionChanged -> {
                updateState { it.copy(deduction = event.value) }
                calculateTotal()
            }

            is OnRateChanged -> {
                updateState { it.copy(rate = event.value) }
                calculateTotal()
            }

            is OnAmountPaidChanged -> {
                updateState { it.copy(amountPaid = event.value) }
            }

            is OnNoteChanged -> updateState { it.copy(note = event.value) }
            is OnSaveClicked -> saveSale(true)

            OnDateClick -> emitEffect(SaleFormUiEffect.OpenDatePicker)
//            OnPaymentDateClick -> emitEffect(SaleFormUiEffect.OpenPaymentDatePicker)
            is LoadSaleForEdit -> loadSaleForEdit(event.saleId)
            OnSaveAndNewClicked -> {
                saveSale(false)
            }

            is OnDeleteClicked -> {
                saleId?.let {
                    onDeleteSaleClicked(saleId)
                    emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Deleted Successfully"))
                }
            }
        }
    }


    fun onDeleteSaleClicked(saleId: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            try {
                deleteSaleUseCase(saleId)

            } catch (e: Exception) {
                emitEffect(SaleFormUiEffect.ShowSnackbar("Error: ${e.message}"))
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    private fun fetchBalance(accountId: String) {
        balanceJob?.cancel()
        balanceJob = viewModelScope.launch {
            repository.getAccountBalance(accountId).collect { balance ->
                updateState { it.copy(currentBalance = balance) }
            }
        }
    }

    private fun calculateTotal() {
        val state = currentState
        val vol = state.volume.toDoubleOrNull() ?: 0.0
        val ded = state.deduction.toDoubleOrNull() ?: 0.0
        val rate = state.rate.toDoubleOrNull() ?: 0.0
        val total = MilkCalculationUtils.calculateCustomerPrice(vol, ded, rate)
        updateState { it.copy(calculatedTotal = total) }
    }

    private fun saveSale(exitAfterSave: Boolean) {
        val state = currentState

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

        // ✅ VALIDATION: Payment hai tu Date LAZMI hai
//        if (payment > 0) {
//            if (state.paymentDate == null) {
//                emitEffect(SaleFormUiEffect.ShowSnackbar("⚠️ Payment Date select karna zaroori hai!"))
//                return
//            }
//        }

        // Agar date select nahi ki (amount 0 k case me), to NULL hi pass hoga.
        val finalPaymentDate = state.paymentDate

        val rawRate = state.rate.toDoubleOrNull() ?: 0.0
        val finalRate = if (rawRate.isNaN() || rawRate.isInfinite()) 0.0 else rawRate
        val ded = state.deduction.toDoubleOrNull() ?: 0.0

        if (ded > vol) {
            emitEffect(SaleFormUiEffect.ShowSnackbar("Deduction cannot be greater than Volume"))
            return
        }

        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            try {
                if (saleId != null) {
                    // === EDIT MODE ===
                    val updateRequest = UpdateSaleRequest(
                        saleId = saleId,
                        accountId = state.selectedCustomer.accountId,
                        date = state.date,

                        paymentDate = finalPaymentDate, // ✅ Passed as Nullable

                        volume = vol,
                        deduction = ded,
                        rate = finalRate,
                        amountPaid = (payment * 100).toLong(),
                        note = state.note
                    )
                    updateSaleUseCase(updateRequest)
                    emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Updated Successfully"))
                    emitEffect(SaleFormUiEffect.NavigateBack)

                } else {
                    // === NEW ENTRY MODE ===
                    saveSaleUseCase(
                        accountId = state.selectedCustomer!!.accountId,
                        volume = vol,
                        deduction = ded,
                        rate = finalRate,
                        amountPaid = (payment * 100).toLong(),
                        note = state.note,
                        saleDate = state.date,

                        paymentDate = finalPaymentDate // ✅ Passed as Nullable
                    )
                    emitEffect(SaleFormUiEffect.ShowSnackbar("Sale Saved Successfully"))

                    if (exitAfterSave) {
                        emitEffect(SaleFormUiEffect.NavigateBack)
                    } else {
                        resetFormForNewEntry()
                    }
                }

            } catch (e: Exception) {
                Log.e("SaleFormViewModel", "Error saving sale", e)
                emitEffect(SaleFormUiEffect.ShowSnackbar("Error: ${e.message}"))
            } finally {
                updateState { it.copy(isSaving = false) }
            }
        }
    }

    private fun resetFormForNewEntry() {
        balanceJob?.cancel()
        updateState {
            it.copy(
                selectedCustomer = null,
                volume = "",
                deduction = "",
                amountPaid = "",
                note = "",
                rate = "",
                currentBalance = 0L,
                calculatedTotal = 0.0,

                isEditMode = false,

                // ✅ RESET: Agli entry k liye phir se NULL
//                paymentDate = null
            )
        }
    }
}
