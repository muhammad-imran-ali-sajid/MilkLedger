package com.miassolutions.milkledger.presentation.supplier.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val repository: PurchaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    private val _balanceSupplierId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val balanceHistory: StateFlow<List<BalanceHistory>> =
        _balanceSupplierId
            .filterNotNull()
            .flatMapLatest { repository.getBalanceHistory(it) }
            .onStart { emit(emptyList()) }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    init {
        observeForDate(_uiState.value.currentDate)
    }

    fun setBalanceSupplierId(supplierId: String) {
        _balanceSupplierId.value = supplierId
    }

    fun deletePurchase(purchaseId: String) {
        viewModelScope.launch {

            repository.deletePurchase(purchaseId)
        }
    }

    fun updatePurchaseManually(updated: PurchaseEntity) {
        viewModelScope.launch {
            val newTs = MilkCalculationUtils.calculateTS(
                fat = updated.fat,
                lr = updated.lr,
                volume = updated.milkAmount
            )

            val newPrice = MilkCalculationUtils.calculatePrice(
                rate = updated.rateUsed,
                volume = updated.milkAmount,
                fat = updated.fat,
                lr = updated.lr
            )

            val newBalance = updated.payment - newPrice

            val finalEntry = updated.copy(
                ts = newTs,
                milkPrice = newPrice,
                balance = newBalance
            )

            repository.updatePurchase(finalEntry)
        }
    }

    fun onDateSelected(date: LocalDate) {
        if (date != _uiState.value.currentDate) {
            _uiState.update { it.copy(currentDate = date) }
            observeForDate(date)
        }
    }

    // ----------------------------------------------------------------
    // ✔ Clean version: Only loads purchases for the selected date
    // ----------------------------------------------------------------
    private var purchasesJob: Job? = null

    fun observeForDate(date: LocalDate) {
        purchasesJob?.cancel()
        purchasesJob = viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

            repository.getPurchasesByDate(date).collectLatest { list ->

                val sorted = list.sortedBy { it.supplier.sortOrder }

                val totalVolume = sorted.sumOf { it.purchase.milkAmount }

                val avgFat = repository.getAvgFat(date).first() ?: 0.0
                val avgLr = repository.getAvgLr(date).first() ?: 0.0
                val avgTS = repository.getAvgTs(date).first() ?: 0.0
                val volumeWithFatLr = repository.getTotalMilkWithFatLR(date).first() ?: 0.0

                val grandTotal = sorted.sumOf { it.purchase.milkPrice }
                val avgRatePerLiter =
                    if (totalVolume > 0) grandTotal / totalVolume else 0.0

                _uiState.update {
                    it.copy(
                        currentDate = date,
                        purchasesForDate = sorted,
                        totalVolume = totalVolume,
                        avgFat = avgFat,
                        avgLr = avgLr,
                        totalTS = avgTS,
                        volumeWithFatLr = volumeWithFatLr,
                        grandTotalForDate = grandTotal,
                        avgRatePerLiter = avgRatePerLiter,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: PurchaseUiEvent) {
        when (event) {
            is PurchaseUiEvent.OnSupplierSelected ->
                _uiState.update { it.copy(navigateToLedgerForSupplierId = event.supplierId) }

            is PurchaseUiEvent.SelectDate ->
                onDateSelected(event.date)
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navigateToLedgerForSupplierId = null) }
    }
}
