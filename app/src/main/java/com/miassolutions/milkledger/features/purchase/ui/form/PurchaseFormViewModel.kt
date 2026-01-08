package com.miassolutions.milkledger.features.purchase.purchaseform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.features.purchase.domain.SavePurchaseUseCase
import com.miassolutions.milkledger.features.purchase.ui.form.PurchaseFormUiEffect
import com.miassolutions.milkledger.features.purchase.ui.form.PurchaseFormUiEvent
import com.miassolutions.milkledger.features.purchase.ui.form.PurchaseFormUiState
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PurchaseFormViewModel @Inject constructor(
    private val repository: MilkPurchaseRepository,
    private val savePurchaseUseCase: SavePurchaseUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PurchaseFormUiState, PurchaseFormUiEvent, PurchaseFormUiEffect>(PurchaseFormUiState()) {

    private val purchaseId: String? = savedStateHandle["purchaseId"]

    // Suppliers List (For Dropdown)
    val suppliersList = repository.getSuppliers() // Note: Repository me getSuppliers (AccountType.SUPPLIER) bana len

    init {
        if (purchaseId != null) {
            loadPurchaseForEdit(purchaseId)
        } else {
            // New Entry Defaults
            updateState { it.copy(date = LocalDate.now(), paymentDate = LocalDate.now()) }
        }
    }

    private fun loadPurchaseForEdit(id: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val purchase = repository.getPurchaseById(id)

            if (purchase != null) {
                // Supplier dhoondein
                val allSuppliers = repository.getSuppliers().firstOrNull() ?: emptyList()
                val supplier = allSuppliers.find { it.accountId == purchase.supplierId }

                updateState {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,
                        selectedSupplier = supplier,
                        date = purchase.dateMillis.toLocalDate(),
                        paymentDate = purchase.paymentDateMillis?.toLocalDate() ?: purchase.dateMillis.toLocalDate(),

                        volume = purchase.volume.toString(),
                        fat = if (purchase.fat > 0) purchase.fat.toString() else "", // 0 ko empty dikhayen
                        lr = if (purchase.lr > 0) purchase.lr.toString() else "",
                        rate = purchase.rate.toString(),

                        amountPaid = (purchase.paymentMade / 100.0).toString(),
                        note = purchase.note ?: ""
                    )
                }
                // Balance load aur calculation refresh
                if (supplier != null) fetchBalance(supplier.accountId)
                calculateLiveValues()
            } else {
                emitEffect(PurchaseFormUiEffect.ShowSnackbar("Record not found"))
                emitEffect(PurchaseFormUiEffect.NavigateBack)
            }
        }
    }

    override fun onEvent(event: PurchaseFormUiEvent) {
        when (event) {
            // --- Inputs ---
            is PurchaseFormUiEvent.OnSupplierSelected -> {
                updateState {
                    it.copy(
                        selectedSupplier = event.supplier,
                        rate = event.supplier.defaultRate.toString()
                    )
                }
                fetchBalance(event.supplier.accountId)
                calculateLiveValues()
            }
            is PurchaseFormUiEvent.OnVolumeChanged -> {
                updateState { it.copy(volume = event.value) }
                calculateLiveValues()
            }
            is PurchaseFormUiEvent.OnFatChanged -> {
                updateState { it.copy(fat = event.value) }
                calculateLiveValues()
            }
            is PurchaseFormUiEvent.OnLrChanged -> {
                updateState { it.copy(lr = event.value) }
                calculateLiveValues()
            }
            is PurchaseFormUiEvent.OnRateChanged -> {
                updateState { it.copy(rate = event.value) }
                calculateLiveValues()
            }
            is PurchaseFormUiEvent.OnAmountPaidChanged -> {
                updateState { it.copy(amountPaid = event.value) }
            }
            is PurchaseFormUiEvent.OnNoteChanged -> updateState { it.copy(note = event.value) }

            // --- Dates ---
            is PurchaseFormUiEvent.OnDateSelected -> updateState { it.copy(date = event.date) }
            is PurchaseFormUiEvent.OnPaymentDateSelected -> updateState { it.copy(paymentDate = event.date) }
            PurchaseFormUiEvent.OnDateClick -> emitEffect(PurchaseFormUiEffect.OpenDatePicker)
            PurchaseFormUiEvent.OnPaymentDateClick -> emitEffect(PurchaseFormUiEffect.OpenPaymentDatePicker)

            // --- Actions ---
            PurchaseFormUiEvent.OnSaveClicked -> savePurchase()
            PurchaseFormUiEvent.OnBackClicked -> emitEffect(PurchaseFormUiEffect.NavigateBack)
        }
    }

    // 🔥 LIVE CALCULATION LOGIC
    private fun calculateLiveValues() {
        val s = currentState
        val vol = s.volume.toDoubleOrNull() ?: 0.0
        val fat = s.fat.toDoubleOrNull() ?: 0.0
        val lr = s.lr.toDoubleOrNull() ?: 0.0
        val rate = s.rate.toDoubleOrNull() ?: 0.0

        // 1. Calculate TS (UI par dikhane k liye)
        val ts = MilkCalculationUtils.calculateTS(fat, lr, vol)

        // 2. Calculate Price (Apke Utils ki logic use hogi: Agar fat/lr 0 hain to flat, warna TS base)
        val total = MilkCalculationUtils.calculatePrice(vol, fat, lr, rate)

        updateState {
            it.copy(
                calculatedTs = ts,
                calculatedTotal = total
            )
        }
    }

    private fun fetchBalance(accountId: String) {
        viewModelScope.launch {
            repository.getAccountBalance(accountId).collect { balance ->
                updateState { it.copy(currentBalance = balance) }
            }
        }
    }

    private fun savePurchase() {
        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            val s = currentState

            val result = savePurchaseUseCase(
                isEditMode = purchaseId != null,
                purchaseId = purchaseId,
                supplier = s.selectedSupplier,
                date = s.date,
                paymentDate = s.paymentDate,
                volumeStr = s.volume,
                fatStr = s.fat, // UseCase khud 0 handle karega
                lrStr = s.lr,   // UseCase khud 0 handle karega
                rateStr = s.rate,
                paymentStr = s.amountPaid,
                note = s.note
            )

            result.onSuccess { msg ->
                emitEffect(PurchaseFormUiEffect.ShowSnackbar(msg))
                emitEffect(PurchaseFormUiEffect.NavigateBack)
            }.onFailure { e ->
                emitEffect(PurchaseFormUiEffect.ShowSnackbar(e.message ?: "Error"))
            }

            updateState { it.copy(isSaving = false) }
        }
    }
}