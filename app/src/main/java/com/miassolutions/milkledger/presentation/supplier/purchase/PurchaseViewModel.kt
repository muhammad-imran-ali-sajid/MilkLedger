package com.miassolutions.milkledger.presentation.supplier.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

//            repository.updatePurchase(finalEntry)
        }
    }

    fun goToNextDate() {
        val next = _uiState.value.currentDate.plusDays(1)
        onDateSelected(next)
    }

    fun goToPreviousDate() {
        val prev = _uiState.value.currentDate.minusDays(1)
        onDateSelected(prev)
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
//        purchasesJob = viewModelScope.launch {
//
//            _uiState.update { it.copy(isLoading = true) }
//
//            repository.getPurchasesByDate(date).collectLatest { list ->
//
//                val sorted = list.sortedBy { it.supplier.sortOrder }
//
//                // Cache supplier history to avoid repeated DB queries
//                val historyCache = mutableMapOf<String, List<PurchaseWithSupplier>>()
//
//                val uiList = mutableListOf<PurchaseUi>()
//
//                for (item in sorted) {
//
//                    val supplierId = item.supplier.supplierId
//
//                    // Fetch history from cache or DB (one-time per supplier)
//                    val history = historyCache.getOrPut(supplierId) {
//                        // make sure your repository exposes a suspend function that returns full history
//                        // Example: suspend fun getSupplierHistoryOnce(supplierId: String): List<PurchaseWithSupplier>
//                        repository.getBalanceHistoryOnce(supplierId)
//                    }
//
//                    // Compute running total for this supplier up to the date of this row
//                    var running = 0.0
//                    for (entry in history) {
//                        // include entries on or before this row's date
//                        if (!entry.purchase.date.isAfter(item.purchase.date)) {
//                            running += entry.purchase.balance
//                        } else {
//                            // since history is ordered by date ASC, we can break early
//                            break
//                        }
//                    }
//
//                    // Add a single PurchaseUi for THIS row, using the computed running total
//                    uiList.add(
//                        PurchaseUi(
//                            data = item,
//                            accumulatedBalance = running
//                        )
//                    )
//                }
//
//                val allRunningBalance = uiList.sumOf { it.accumulatedBalance }
//
//                val totalVolume = sorted.sumOf { it.purchase.milkAmount }
//
//                val avgFat = repository.getAvgFat(date).first() ?: 0.0
//                val avgLr = repository.getAvgLr(date).first() ?: 0.0
//                val avgTS = repository.getAvgTs(date).first() ?: 0.0
//                val volumeWithFatLr = repository.getTotalMilkWithFatLR(date).first() ?: 0.0
//
//                val grandTotal = sorted.sumOf { it.purchase.milkPrice }
//                val totalPaid = sorted.sumOf { it.purchase.payment }
//                val avgRatePerLiter = if (totalVolume > 0) grandTotal / totalVolume else 0.0
//
//                _uiState.update {
//                    it.copy(
//                        currentDate = date,
//                        purchasesForDate = sorted,   // List<PurchaseWithSupplier>
//                        purchasesUi = uiList,        // List<PurchaseUi> for adapter
//                        totalVolume = totalVolume,
//                        avgFat = avgFat,
//                        avgLr = avgLr,
//                        totalTS = avgTS,
//                        volumeWithFatLr = volumeWithFatLr,
//                        grandTotalForDate = grandTotal,
//                        totalPaid =totalPaid,
//                        totalBalance = allRunningBalance,
//                        avgRatePerLiter = avgRatePerLiter,
//                        isLoading = false
//                    )
//                }
//            }
//        }
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
