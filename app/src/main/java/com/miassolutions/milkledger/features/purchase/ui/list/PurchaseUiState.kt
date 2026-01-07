package com.miassolutions.milkledger.features.purchase.ui.list

import java.time.LocalDate

data class PurchaseUiState(
    val currentDate: LocalDate = LocalDate.now(),


    val grandTotalForDate: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalBalance: Double = 0.0,
    val selectedSupplierId: String? = null,
    val navigateToLedgerForSupplierId: String? = null,

    val totalVolume: Double = 0.0,
    val avgFat: Double = 0.0,
    val avgLr: Double = 0.0,
    val totalTS: Double = 0.0,
    val volumeWithFatLr: Double = 0.0,
    val avgRatePerLiter: Double = 0.0,

    val isLoading: Boolean = false
)


sealed class PurchaseUiEvent {
    data class OnSupplierSelected(val supplierId: String) : PurchaseUiEvent()
    data class SelectDate(val date: LocalDate) : PurchaseUiEvent()
}
