@file:OptIn(ExperimentalCoroutinesApi::class)

package com.miassolutions.milkledger.features.purchase.ui.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect.NavigateToAddPurchase
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect.NavigateToEditPurchase
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect.OpenBalanceHistorySheet
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect.OpenDatePicker
import com.miassolutions.milkledger.features.purchase.ui.list.PurchaseListUiEffect.OpenSupplierHistory
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MilkPurchaseListViewModel @Inject constructor(
    private val repository: MilkPurchaseRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PurchaseListUiState, PurchaseListUiEvent, PurchaseListUiEffect>(
    PurchaseListUiState()
) {


    private val dateFlow = MutableStateFlow<LocalDate?>(null)

    init {

        observeDataFlow()
    }

    fun setDateAndLoad(dateMillis: Long) {
        // Agar date already loaded hai (Rotation case), to wapis load na karein
        if (dateFlow.value != null) return

        val date = if (dateMillis != -1L) {
            dateMillis.toLocalDate()
        } else {
            LocalDate.now()
        }

        loadPurchases(date)
    }


    private fun observeDataFlow() {
        dateFlow
            .filterNotNull()
            .flatMapLatest { date ->
                combine(
                    repository.getPurchasesByDate(date.toMillis()),
                    repository.getGlobalPurchaseStats(date.toMillis(), date.toMillis())
                ) { purchases, summary ->
                    purchases to summary
                }
            }
            .onEach { (p, s) ->
                updateState {
                    it.copy(
                        isLoading = false,
                        purchases = p,
                        summary = s
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadPurchases(date: LocalDate) {
        updateState { it.copy(isLoading = true, date = date) }
        dateFlow.value = date
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

            is PurchaseListUiEvent.OnSupplierHistoryClick -> {
                emitEffect(OpenSupplierHistory(event.supplierId, event.supplierName))
            }
        }
    }
}