package com.miassolutions.milkledger.presentation.purchase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.repositories.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
        val today = _uiState.value.currentDate
        ensureDailyPurchasesExist(today)
        observePurchasesForDate(_uiState.value.currentDate)
    }

    private fun ensureDailyPurchasesExist(date: LocalDate) {
        viewModelScope.launch {
            // Get all suppliers
            val allSuppliers = repository.getAllSuppliers() // we’ll add this below
            // Get all purchases for today
            val currentPurchases = repository.getPurchasesByDateOnce(date)

            // For suppliers missing an entry today → insert one
            val missingSuppliers = allSuppliers.filterNot { supplier ->
                currentPurchases.any { it.supplier.supplierId == supplier.supplierId }
            }

            missingSuppliers.forEach { supplier ->
                val newEntry = PurchaseEntryEntity(
                    supplierId = supplier.supplierId,
                    date = date,
                    volume = 0.0,
                    fat = 0.0,
                    lr = 0.0,
                    ts = 0.0,
                    price = 0.0,
                    paid = 0.0,
                    balance = 0.0,
                    rateUsed = supplier.supplierRate
                )
                repository.insertPurchase(newEntry)
            }
        }
    }


    // ----------------------------------------------------------
    // 🔄 Observe daily purchases
    // ----------------------------------------------------------
    private fun observePurchasesForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.getPurchasesByDate(date).collectLatest { purchases ->
                Log.d("PurchaseViewModel", "Loaded purchases for $date → ${purchases.size}")
                val grandTotal = purchases.sumOf { it.purchase.price }
                _uiState.update {
                    it.copy(
                        purchasesForDate = purchases,
                        grandTotalForDate = grandTotal
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

            is PurchaseUiEvent.OnSupplierSelected -> {
                _uiState.update { it.copy(navigateToLedgerForSupplierId = event.supplierId) }
            }

            is PurchaseUiEvent.OnVolumeChanged -> updateField(
                entryId = event.entryId,
                volume = event.volume
            )

            is PurchaseUiEvent.OnFatChanged -> updateField(
                entryId = event.entryId,
                fat = event.fat
            )

            is PurchaseUiEvent.OnLrChanged -> updateField(
                entryId = event.entryId,
                lr = event.lr
            )

            is PurchaseUiEvent.OnNotesChanged -> updateField(
                entryId = event.entryId,
                notes = event.notes
            )

            is PurchaseUiEvent.OnPaidChanged -> updateField(
                entryId = event.entryId,
                paid = event.paid
            )
        }
    }

    // ----------------------------------------------------------
    // ✏️ Update entry (auto-save live)
    // ----------------------------------------------------------
    private fun updateField(
        entryId: String,
        volume: Double? = null,
        fat: Double? = null,
        lr: Double? = null,
        paid: Double? = null,
        notes: String? = null
    ) {
        val currentList = _uiState.value.purchasesForDate
        val updated = currentList.map { pws ->
            if (pws.purchase.purchaseId == entryId) {

                // --- Calculate new field values safely ---
                val newVolume = volume ?: pws.purchase.volume
                val newFat = fat ?: pws.purchase.fat
                val newLr = lr ?: pws.purchase.lr
                val newPrice = MilkCalculationUtils.calculatePrice(
                    rate = pws.purchase.rateUsed,
                    volume = newVolume,
                    fat = newFat,
                    lr = newLr
                )

                // if user clears the paid field, treat it as 0.0
                val enteredPaid = paid ?: 0.0
                // avoid paying more than total
                val newPaid = enteredPaid.coerceAtMost(newPrice)

                val newBalance = newPrice - newPaid

                val purchase = pws.purchase.copy(
                    volume = newVolume,
                    fat = newFat,
                    lr = newLr,
                    ts = MilkCalculationUtils.calculateTS(newFat, newLr, newVolume),
                    price = newPrice,
                    paid = newPaid,
                    balance = newBalance,
                    notes = notes ?: pws.purchase.notes
                )

                viewModelScope.launch { repository.updatePurchase(purchase) }
                pws.copy(purchase = purchase)

            } else pws
        }

        val newGrandTotal = updated.sumOf { it.purchase.price }
        _uiState.update {
            it.copy(
                purchasesForDate = updated,
                grandTotalForDate = newGrandTotal
            )
        }
    }



    // ----------------------------------------------------------
    // 🧭 Navigation Reset
    // ----------------------------------------------------------
    fun onLedgerNavigated() {
        _uiState.update { it.copy(navigateToLedgerForSupplierId = null) }
    }
}
