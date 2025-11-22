package com.miassolutions.milkledger.presentation.supplier.purchase

import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import java.time.LocalDate

data class PurchaseUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val purchasesForDate: List<PurchaseWithSupplier> = emptyList(),
    val grandTotalForDate: Double = 0.0,
    val selectedSupplierId: String? = null,
    val navigateToLedgerForSupplierId: String? = null,

    val totalVolume: Double = 0.0,
    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,
    val totalTS : Double = 0.0,
    val volumeWithFatLr : Double = 0.0,
    val avgRatePerLiter: Double = 0.0,
)


sealed class PurchaseUiEvent {
    data class OnSupplierSelected(val supplierId: String) : PurchaseUiEvent()
    data class SelectDate(val date: LocalDate) : PurchaseUiEvent()
}
