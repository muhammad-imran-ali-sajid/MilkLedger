package com.miassolutions.milkledger.presentation.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.data.repositories.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var updateJob: Job? = null

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        observeForDate(_uiState.value.currentDate)
    }

    fun updatePurchaseManually(updated: PurchaseEntryEntity) {
        viewModelScope.launch {
            // 🧮 Recalculate derived values before saving
            val newTs = MilkCalculationUtils.calculateTS(
                fat = updated.fat,
                lr = updated.lr,
                volume = updated.volume
            )

            val newPrice = MilkCalculationUtils.calculatePrice(
                rate = updated.rateUsed,
                volume = updated.volume,
                fat = updated.fat,
                lr = updated.lr
            )

            val newBalance =  updated.paid - newPrice

            val finalEntry = updated.copy(
                ts = newTs,
                price = newPrice,
                balance = newBalance
            )

            repository.updatePurchase(finalEntry)
        }
    }


    fun onDateSelected(date: LocalDate) {
        observeForDate(date)
    }

    // ----------------------------------------------------------
    // 🔁 Observe purchases for selected date
    // ----------------------------------------------------------
    fun observeForDate(date: LocalDate) {
        viewModelScope.launch {
            val sortedSuppliers = repository.getAllSuppliers().first()
            val existingPurchases = repository.getPurchasesByDateOnce(date)

            // Create missing purchase entries
            val missingSuppliers = sortedSuppliers.filterNot { supplier ->
                existingPurchases.any { it.supplier.supplierId == supplier.supplierId }
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

            // Observe updates
            repository.getPurchasesByDate(date).collectLatest { purchases ->
                val sortedPurchases = purchases.sortedBy { it.supplier.sortOrder }

                val totalVolume = sortedPurchases.sumOf { it.purchase.volume }
                val avgFat = if (totalVolume > 0) {
                    sortedPurchases.sumOf { it.purchase.fat * it.purchase.volume } / totalVolume
                } else 0.0
                val avgLr = if (totalVolume > 0) {
                    sortedPurchases.sumOf { it.purchase.lr * it.purchase.volume } / totalVolume
                } else 0.0
                val grandTotal = sortedPurchases.sumOf { it.purchase.price }
                val avgRatePerLiter = if (totalVolume > 0) grandTotal / totalVolume else 0.0

                _uiState.update {
                    it.copy(
                        currentDate = date,
                        purchasesForDate = sortedPurchases,
                        totalVolume = totalVolume,
                        avgFat = avgFat,
                        avgLr = avgLr,
                        grandTotalForDate = grandTotal,
                        avgRatePerLiter = avgRatePerLiter
                    )
                }
            }
        }
    }

    // ----------------------------------------------------------
    // ✏️ Update entry (used by BottomSheet)
    // ----------------------------------------------------------
    fun updatePurchaseEntry(updated: PurchaseEntryEntity) {
        viewModelScope.launch {
            repository.updatePurchase(updated)
        }
    }

    // ----------------------------------------------------------
    // ✏️ Inline field updates (auto-save while typing)
    // ----------------------------------------------------------
    private fun updateField(
        entryId: String,
        volume: Double? = null,
        fat: Double? = null,
        lr: Double? = null,
        paid: Double? = null,
        notes: String? = null
    ) {
        val updatedList = _uiState.value.purchasesForDate.map { item ->
            if (item.purchase.purchaseId == entryId) {
                val newPurchase = recalculatePurchase(item, volume, fat, lr, paid, notes)
                debounceUpdate(newPurchase)
                item.copy(purchase = newPurchase)
            } else item
        }

        val newGrandTotal = updatedList.sumOf { it.purchase.price }
        _uiState.update {
            it.copy(
                purchasesForDate = updatedList,
                grandTotalForDate = newGrandTotal
            )
        }
    }

    private fun recalculatePurchase(
        item: PurchaseWithSupplier,
        volume: Double?,
        fat: Double?,
        lr: Double?,
        paid: Double?,
        notes: String?
    ): PurchaseEntryEntity {
        val v = volume ?: item.purchase.volume
        val f = fat ?: item.purchase.fat
        val l = lr ?: item.purchase.lr
        val p = paid ?: item.purchase.paid

        val newPrice = MilkCalculationUtils.calculatePrice(
            rate = item.purchase.rateUsed,
            volume = v,
            fat = f,
            lr = l
        )

        val newBalance = newPrice - p

        return item.purchase.copy(
            volume = v,
            fat = f,
            lr = l,
            ts = MilkCalculationUtils.calculateTS(f, l, v),
            price = newPrice,
            paid = p,
            balance = newBalance,
            notes = notes ?: item.purchase.notes
        )
    }

    private fun debounceUpdate(purchase: PurchaseEntryEntity) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            delay(400)
            repository.updatePurchase(purchase)
        }
    }

    // ----------------------------------------------------------
    // ⚡ Handle UI Events
    // ----------------------------------------------------------
    fun onEvent(event: PurchaseUiEvent) {
        when (event) {
            is PurchaseUiEvent.OnSupplierSelected ->
                _uiState.update { it.copy(navigateToLedgerForSupplierId = event.supplierId) }

            is PurchaseUiEvent.OnVolumeChanged ->
                updateField(event.entryId, volume = event.volume)

            is PurchaseUiEvent.OnFatChanged ->
                updateField(event.entryId, fat = event.fat)

            is PurchaseUiEvent.OnLrChanged ->
                updateField(event.entryId, lr = event.lr)

            is PurchaseUiEvent.OnPaidChanged ->
                updateField(event.entryId, paid = event.paid)

            is PurchaseUiEvent.OnNotesChanged ->
                updateField(event.entryId, notes = event.notes)
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navigateToLedgerForSupplierId = null) }
    }
}
