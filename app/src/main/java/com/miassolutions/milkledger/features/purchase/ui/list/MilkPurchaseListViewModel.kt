package com.miassolutions.milkledger.features.purchase.ui.list

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect.*
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MilkPurchaseListViewModel @Inject constructor(
    private val repository: MilkPurchaseRepository
) : BaseViewModel<PurchaseListUiState, PurchaseListUiEvent, PurchaseListUiEffect>(
    PurchaseListUiState()
) {

    // Job variable to track the current flow
    private var searchJob: Job? = null

    init {
        loadPurchases(LocalDate.now())
    }

    private fun loadPurchases(date: LocalDate) {
        // 1. Purani job cancel karein taake conflicts na hon
        searchJob?.cancel()

        updateState { it.copy(isLoading = true, date = date) }

        // 2. Naya flow start karein
        searchJob = repository.getPurchasesByDate(date.toMillis())
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

    private fun deletePurchase(id: String) {
        viewModelScope.launch {
            try {
                // repository.deletePurchase(id) // Uncomment when repo is ready
                emitEffect(ShowSnackbar("Deleted"))
            } catch (e: Exception) {
                emitEffect(ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    override fun onEvent(event: PurchaseListUiEvent) {
        when (event) {
            is PurchaseListUiEvent.OnDateSelected -> {
                loadPurchases(event.date)
            }
            PurchaseListUiEvent.OnDateClick -> emitEffect(OpenDatePicker)
            PurchaseListUiEvent.OnAddPurchaseClick -> emitEffect(NavigateToAddPurchase)

            is PurchaseListUiEvent.OnEditClick ->
                emitEffect(NavigateToEditPurchase(event.purchaseId))

            is PurchaseListUiEvent.OnBalanceClick -> {
                emitEffect(OpenBalanceHistorySheet(event.supplierId, event.supplierName))
            }

            is PurchaseListUiEvent.OnDeleteClick -> deletePurchase(event.purchaseId)
            is PurchaseListUiEvent.OnSupplierHistoryClick -> {
                emitEffect(OpenSupplierHistory(event.supplierId, event.supplierName))
            }
        }
    }
}