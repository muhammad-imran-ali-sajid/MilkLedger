package com.miassolutions.milkledger.presentation.supplier.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.repositories.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
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

    init {
        observeForDate(_uiState.value.currentDate)
    }

    fun updatePurchaseManually(updated: PurchaseEntity) {
        viewModelScope.launch {
            // 🧮 Recalculate derived values before saving
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
            observeForDate(date)
        }
    }


    // ----------------------------------------------------------
    // 🔁 Observe purchases for selected date
    // ----------------------------------------------------------
    private var purchasesJob: Job? = null

    fun observeForDate(date: LocalDate) {
        purchasesJob?.cancel()
        purchasesJob = viewModelScope.launch {
            val sortedSuppliers = repository.getAllSuppliers().first()
            val existingPurchases = repository.getPurchasesByDateOnce(date)

            val missingSuppliers = sortedSuppliers.filterNot { supplier ->
                existingPurchases.any { it.supplier.supplierId == supplier.supplierId }
            }

            missingSuppliers.forEach { supplier ->
                val newEntry = PurchaseEntity(
                    supplierId = supplier.supplierId,
                    date = date,
                    milkAmount = 0.0,
                    fat = 0.0,
                    lr = 0.0,
                    ts = 0.0,
                    milkPrice = 0.0,
                    payment = 0.0,
                    balance = 0.0,
                    rateUsed = supplier.supplierRate
                )
                repository.insertPurchase(newEntry)
            }

            repository.getPurchasesByDate(date).collectLatest { purchases ->
                val sortedPurchases = purchases.sortedBy { it.supplier.sortOrder }
                val validTsEntries = sortedPurchases.filter { it.purchase.ts > 0.0 }
                val validFatEntries = sortedPurchases.filter { it.purchase.fat > 0.0 }
                val validLREntries = sortedPurchases.filter { it.purchase.lr > 0.0 }

                val totalVolume = sortedPurchases.sumOf { it.purchase.milkAmount }
                val avgFat = if (validFatEntries.isNotEmpty()) {
                    val totalFatMilk =
                        validFatEntries.sumOf { it.purchase.fat * it.purchase.milkAmount }
                    val totalFatVolume = validFatEntries.sumOf { it.purchase.milkAmount }
                    totalFatMilk / totalFatVolume
                } else 0.0

                val avgLr = if (validLREntries.isNotEmpty()) {
                    val totalLRMilk =
                        validLREntries.sumOf { it.purchase.lr * it.purchase.milkAmount }
                    val totalLRVolume = validFatEntries.sumOf { it.purchase.milkAmount }
                    totalLRMilk / totalLRVolume
                } else 0.0


                val avgTS = if (validTsEntries.isNotEmpty()) {
                    val totalTsMilk =
                        validTsEntries.sumOf { it.purchase.ts }
                    totalTsMilk / validTsEntries.count()
                } else 0.0
                val grandTotal = sortedPurchases.sumOf { it.purchase.milkPrice }
                val avgRatePerLiter = if (totalVolume > 0) grandTotal / totalVolume else 0.0

                _uiState.update {
                    it.copy(
                        currentDate = date,
                        purchasesForDate = sortedPurchases,
                        totalVolume = totalVolume,
                        avgFat = avgFat,
                        avgLr = avgLr,
                        avgTS = avgTS,
                        grandTotalForDate = grandTotal,
                        avgRatePerLiter = avgRatePerLiter
                    )
                }
            }
        }
    }


    // ----------------------------------------------------------
    // ⚡ Handle UI Events
    // ----------------------------------------------------------
    fun onEvent(event: PurchaseUiEvent) {
        when (event) {
            is PurchaseUiEvent.OnSupplierSelected ->
                _uiState.update { it.copy(navigateToLedgerForSupplierId = event.supplierId) }

            is PurchaseUiEvent.SelectDate -> {
                _uiState.update { it.copy(currentDate = event.date) }
            }
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navigateToLedgerForSupplierId = null) }
    }
}
