package com.miassolutions.milkledger.presentation.supplier.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // 2. Public StateFlow (Derived Data)
    // We start with flatMapLatest to switch to the new data stream when the ID changes.
    val balanceHistory: StateFlow<List<BalanceHistory>> = _balanceSupplierId
        .filterNotNull() // Only process non-null IDs
        .flatMapLatest { supplierId ->
            // Call the repository function that now returns Flow
            repository.getBalanceHistory(supplierId)
        }
        // Ensure it starts with an initial value (an empty list)
        .onStart { emit(emptyList()) }
        // Convert the Flow into a StateFlow that shares the results
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Start collecting when a UI collector appears
            initialValue = emptyList()
        )

    // 3. Public function for the Fragment to set the ID
    fun setBalanceSupplierId(supplierId: String) {
        // Update the StateFlow, which automatically triggers the flatMapLatest block above.
        _balanceSupplierId.value = supplierId
    }



    init {
        // Start observing for the initial date from the UI state
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


    /**
     * FIX 1: Ensure the UI state is updated immediately when the date changes,
     * and THEN call observeForDate. This keeps the UI state and the observation
     * logic synchronized.
     */
    fun onDateSelected(date: LocalDate) {
        if (date != _uiState.value.currentDate) {
            // Update the state first
            _uiState.update { it.copy(currentDate = date) }

            // Now start the new observation
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
            val existingPurchasesWithSupplier = repository.getPurchasesByDateOnce(date)

            // Map suppliers for easy lookup of the current rate
            val supplierRateMap = sortedSuppliers.associateBy { it.supplierId }

            // 1. Rate update logic (Only for today, previously added for rate change fix)
            if (date == LocalDate.now()) {
                val updates = existingPurchasesWithSupplier.mapNotNull { purchaseWithSupplier ->
                    val supplier = supplierRateMap[purchaseWithSupplier.purchase.supplierId]
                    val purchaseEntity = purchaseWithSupplier.purchase

                    if (supplier != null && purchaseEntity.rateUsed != supplier.supplierRate) {

                        val newTs = MilkCalculationUtils.calculateTS(
                            fat = purchaseEntity.fat,
                            lr = purchaseEntity.lr,
                            volume = purchaseEntity.milkAmount
                        )

                        val newPrice = MilkCalculationUtils.calculatePrice(
                            rate = supplier.supplierRate,
                            volume = purchaseEntity.milkAmount,
                            fat = purchaseEntity.fat,
                            lr = purchaseEntity.lr
                        )

                        val newBalance = purchaseEntity.payment - newPrice

                        purchaseEntity.copy(
                            rateUsed = supplier.supplierRate,
                            ts = newTs,
                            milkPrice = newPrice,
                            balance = newBalance
                        )
                    } else null
                }

                updates.forEach { repository.updatePurchase(it) }
            }


            // 2. Insert missing entries (original logic)
            val missingSuppliers = sortedSuppliers.filterNot { supplier ->
                existingPurchasesWithSupplier.any { it.purchase.supplierId == supplier.supplierId }
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

            /**
             * FIX 2: Check the date parameter passed to getPurchasesByDate(date)
             * in your PurchaseRepository implementation to ensure it is using the 'date'
             * parameter and not a static or cached date.
             */
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
                    val totalTsMilk = validTsEntries.sumOf { it.purchase.ts }
                    totalTsMilk
                } else 0.0
                val grandTotal = sortedPurchases.sumOf { it.purchase.milkPrice }
                val avgRatePerLiter = if (totalVolume > 0) grandTotal / totalVolume else 0.0

                _uiState.update {
                    // Update all calculated values and ensure currentDate is correct
                    it.copy(
                        currentDate = date,
                        purchasesForDate = sortedPurchases,
                        totalVolume = totalVolume,
                        avgFat = avgFat,
                        avgLr = avgLr,
                        totalTS = avgTS,
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

            // Removed direct date update here as it's now handled in onDateSelected
            is PurchaseUiEvent.SelectDate -> onDateSelected(event.date)
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navigateToLedgerForSupplierId = null) }
    }
}