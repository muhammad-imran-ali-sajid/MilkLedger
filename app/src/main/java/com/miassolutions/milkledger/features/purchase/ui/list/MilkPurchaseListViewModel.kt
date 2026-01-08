package com.miassolutions.milkledger.features.purchase.ui.list


import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MilkPurchaseListViewModel @Inject constructor(
    private val repository: MilkPurchaseRepository
) : BaseViewModel<PurchaseListUiState, PurchaseListUiEvent, PurchaseListUiEffect>(PurchaseListUiState()) {

    init {
        loadPurchases(LocalDate.now())
    }

    private fun loadPurchases(date: LocalDate) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true, date = date) }

            repository.getPurchasesByDate(date.toMillis())
                .onEach { list ->
                    // Summary Calculation
                    val totalVol = list.sumOf { it.volume }
                    val totalPrice = list.sumOf { it.totalAmount }
                    val totalPaid = list.sumOf { it.paymentMade }

                    updateState {
                        it.copy(
                            isLoading = false,
                            purchases = list,
                            totalVolume = totalVol,
                            totalPrice = totalPrice,
                            totalPaid = totalPaid
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    // Delete Logic (Optional - Agar list me delete option ho)
    private fun deletePurchase(id: String) {
        viewModelScope.launch {
            try {
                // repository.deletePurchase(id) // Implement delete in Repo
                emitEffect(PurchaseListUiEffect.ShowSnackbar("Deleted"))
            } catch (e: Exception) {
                emitEffect(PurchaseListUiEffect.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    override fun onEvent(event: PurchaseListUiEvent) {
        when(event) {
            is PurchaseListUiEvent.OnDateSelected -> loadPurchases(event.date)
            PurchaseListUiEvent.OnDateClick -> emitEffect(PurchaseListUiEffect.OpenDatePicker)
            PurchaseListUiEvent.OnAddPurchaseClick -> emitEffect(PurchaseListUiEffect.NavigateToAddPurchase)

            is PurchaseListUiEvent.OnEditClick ->
                emitEffect(PurchaseListUiEffect.NavigateToEditPurchase(event.purchaseId))

            is PurchaseListUiEvent.OnSupplierHistoryClick ->
                emitEffect(PurchaseListUiEffect.NavigateToSupplierHistory(event.supplierId, event.supplierName))

            is PurchaseListUiEvent.OnDeleteClick -> deletePurchase(event.purchaseId)
        }
    }
}