package com.miassolutions.milkledger.features.purchase.ui.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.features.purchase.domain.SavePurchaseUseCase
import com.miassolutions.milkledger.features.purchase.model.SupplierDropDownUiModel
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.extensions.toPrice
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
class PurchaseFormViewModel @Inject constructor(
    private val repository: MilkPurchaseRepository,
    private val savePurchaseUseCase: SavePurchaseUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PurchaseFormUiState, PurchaseFormUiEvent, PurchaseFormUiEffect>(
    PurchaseFormUiState()
) {

    private var balanceJob: Job? = null
    private val purchaseId: String? = savedStateHandle["purchaseId"]
    private val purchaseDate: Long? = savedStateHandle["purchaseDate"]

    private val purchaseDateLocal = purchaseDate?.toLocalDate() ?: LocalDate.now()

    private val _suppliersDropDown = MutableStateFlow<List<SupplierDropDownUiModel>>(emptyList())
    val suppliersDropDown = _suppliersDropDown.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun monitorSuppliersStatus() {
        viewModelScope.launch {
            combine(
                repository.getSuppliers(),
                uiState.map { it.date }.distinctUntilChanged().flatMapLatest { date ->
                    repository.getSuppliersWithPurchaseOnDate(date.toMillis())
                }
            ) { suppliers, completedIds ->
                suppliers.map { account ->
                    SupplierDropDownUiModel(
                        account = account,
                        isEntryDoneToday = completedIds.contains(account.accountId)
                    )
                }
            }.collect { mappedList ->
                _suppliersDropDown.value = mappedList
            }
        }
    }

    init {
        monitorSuppliersStatus()

        if (purchaseId != null) {
            loadPurchaseForEdit(purchaseId)
        } else {
            // New Entry: Payment Date Null (Validation Trigger karne k liye)
            updateState {
                it.copy(
                    date = purchaseDateLocal,
                    paymentDate = purchaseDateLocal
                )
            }
        }
    }

    private fun loadPurchaseForEdit(id: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            val purchase = repository.getPurchaseById(id)

            if (purchase != null) {
                val allSuppliers = repository.getSuppliers().firstOrNull() ?: emptyList()
                val supplier = allSuppliers.find { it.accountId == purchase.supplierId }

                // 🔥 REPOSITORY ALIGNMENT:
                // Ab hum DB se 'paymentDateMillis' direct utha rahay hain.
                // Agar record me date hai to wo load hogi, warna null.
                val savedPaymentDate = purchase.dateMillis.toLocalDate()

                updateState {
                    it.copy(
                        isLoading = false,
                        isEditMode = true,
                        selectedSupplier = supplier,
                        date = purchase.dateMillis.toLocalDate(),

                        advance = supplier?.advanceAmount?.toPrice(),

                        // ✅ DB se Load ki hui date set karen
                        paymentDate = savedPaymentDate,

                        volume = purchase.volume.toString(),
                        fat = if (purchase.fat > 0) purchase.fat.toString() else "",
                        lr = if (purchase.lr > 0) purchase.lr.toString() else "",
                        rate = purchase.rate.toString(),

                        amountPaid = (purchase.paymentMade / 100.0).toString(),
                        note = purchase.note ?: ""
                    )
                }

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
            is PurchaseFormUiEvent.OnSupplierSelected -> {
                updateState {
                    it.copy(
                        selectedSupplier = event.supplier,
                        rate = event.supplier.defaultRate.toString(),
                        advance = event.supplier.advanceAmount?.toPrice()
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

            is PurchaseFormUiEvent.OnDateSelected -> updateState { it.copy(date = event.date) }
//            is PurchaseFormUiEvent.OnPaymentDateSelected -> updateState { it.copy(paymentDate = event.date) }
            PurchaseFormUiEvent.OnDateClick -> emitEffect(PurchaseFormUiEffect.OpenDatePicker)
//            PurchaseFormUiEvent.OnPaymentDateClick -> emitEffect(PurchaseFormUiEffect.OpenPaymentDatePicker)

            PurchaseFormUiEvent.OnSaveClicked -> savePurchase(exitAfterSave = true)
            PurchaseFormUiEvent.OnSaveAndNewClicked -> savePurchase(exitAfterSave = false)
            PurchaseFormUiEvent.OnBackClicked -> emitEffect(PurchaseFormUiEffect.NavigateBack)
            is PurchaseFormUiEvent.OnDeleteClicked -> {
                purchaseId?.let {
                    deletePurchase(purchaseId)
                    emitEffect(PurchaseFormUiEffect.ShowSnackbar("Purchase deleted"))
                }
            }
        }
    }

    private fun deletePurchase(purchaseId: String) {
        viewModelScope.launch {
            repository.deletePurchase(purchaseId)
        }
    }

    private fun calculateLiveValues() {
        val s = currentState
        val vol = s.volume.toDoubleOrNull() ?: 0.0
        val fat = s.fat.toDoubleOrNull() ?: 0.0
        val lr = s.lr.toDoubleOrNull() ?: 0.0
        val rate = s.rate.toDoubleOrNull() ?: 0.0

        val ts = MilkCalculationUtils.calculateTS(fat, lr, vol)
        val total = MilkCalculationUtils.calculatePrice(vol, fat, lr, rate)

        updateState {
            it.copy(
                calculatedTs = ts,
                calculatedTotal = total.toLongPaisa()
            )
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


    private fun savePurchase(exitAfterSave: Boolean) {
        viewModelScope.launch {
            updateState { it.copy(isSaving = true) }
            val s = currentState

            if (s.selectedSupplier == null) {
                emitEffect(PurchaseFormUiEffect.ShowSnackbar("Select Supplier"))
                updateState { it.copy(isSaving = false) }
                return@launch
            }

            // Validation: Force User to Select Date if Amount > 0
            val amount = s.amountPaid.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
                if (s.paymentDate == null) {
                    emitEffect(PurchaseFormUiEffect.ShowSnackbar("⚠️ Payment Date select karna zaroori hai!"))
                    updateState { it.copy(isSaving = false) }
                    return@launch
                }
            }

            // Fallback (Safe side: Agar date null hai (amount 0 k case me) tu Purchase Date use kr lo)
            val finalPaymentDate = s.paymentDate

            val result = savePurchaseUseCase(
                isEditMode = purchaseId != null,
                purchaseId = purchaseId,
                supplier = s.selectedSupplier,
                date = s.date,
                paymentDate = finalPaymentDate, // ✅ Correct Date Passed
                volumeStr = s.volume,
                fatStr = s.fat,
                lrStr = s.lr,
                rateStr = s.rate,
                paymentStr = s.amountPaid,
                note = s.note
            )

            result.onSuccess { msg ->
                emitEffect(PurchaseFormUiEffect.ShowSnackbar(msg))
                if (purchaseId != null) {
                    emitEffect(PurchaseFormUiEffect.NavigateBack)
                } else {
                    if (exitAfterSave) {
                        emitEffect(PurchaseFormUiEffect.NavigateBack)
                    } else {
                        resetFormForNewEntry()
                    }
                }
            }.onFailure { e ->
                emitEffect(PurchaseFormUiEffect.ShowSnackbar(e.message ?: "Error"))
            }

            updateState { it.copy(isSaving = false) }
        }
    }

    private fun resetFormForNewEntry() {
        balanceJob?.cancel()
        updateState {
            it.copy(
                selectedSupplier = null,
                volume = "",
                fat = "",
                lr = "",
                amountPaid = "",
                note = "",
                rate = "",
                currentBalance = 0L,
                calculatedTs = 0.0,
                calculatedTotal = 0L,
                isEditMode = false,
//                paymentDate = null // ✅ Reset to Null
            )
        }
    }
}