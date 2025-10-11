package com.miassolutions.milkledger.presentation.purchase

import com.miassolutions.milkledger.data.local.entities.PurchaseEntryEntity
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import java.time.LocalDate

data class PurchaseUiState(
    val currentDate: LocalDate = LocalDate.now(),
    val purchasesForDate: List<PurchaseWithSupplier> = emptyList(),
    val grandTotalForDate: Double = 0.0,
    val selectedSupplierId: String? = null,
    val navigateToLedgerForSupplierId: String? = null
)


sealed class PurchaseUiEvent {
    data class OnSupplierSelected(val supplierId: String) : PurchaseUiEvent()
    data class OnVolumeChanged(val entryId: String, val volume: Double) : PurchaseUiEvent()
    data class OnFatChanged(val entryId: String, val fat: Double) : PurchaseUiEvent()
    data class OnLrChanged(val entryId: String, val lr: Double) : PurchaseUiEvent()
    data class OnNotesChanged(val entryId: String, val notes: String) : PurchaseUiEvent()
}
