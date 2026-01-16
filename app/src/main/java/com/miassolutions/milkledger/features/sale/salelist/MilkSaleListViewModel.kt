@file:OptIn(ExperimentalCoroutinesApi::class)

package com.miassolutions.milkledger.features.sale.salelist

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEffect.NavigateToAddSale
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEffect.NavigateToCustomerLedger
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEffect.NavigateToEditSale
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEffect.OnDateClick
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
class MilkSaleListViewModel @Inject constructor(
    private val repository: MilkSaleRepository
) : BaseViewModel<MilkSaleListUiState, MilkSaleListUiEvent, MilkSaleListUiEffect>(
    MilkSaleListUiState()
) {

    private val dateFlow = MutableStateFlow<LocalDate?>(null)

    init {
        dateFlow
            .filterNotNull()
            .flatMapLatest { date ->
                combine(
                    repository.getSalesByDate(date.toMillis()),
                    repository.getGlobalSaleStats(date.toMillis(), date.toMillis())
                ) { sales, summary ->
                    sales to summary
                }
            }
            .onEach { (sales, summary) ->
                updateState { it.copy(isLoading = false, sales = sales, summary = summary) }
            }
            .launchIn(viewModelScope)


    }

    override fun onEvent(event: MilkSaleListUiEvent) {
        when (event) {
            // --- Date Navigation ---
            MilkSaleListUiEvent.OnNextDate -> {
                val nextDate = currentState.date.plusDays(1)
                loadSalesForDate(nextDate)
            }

            MilkSaleListUiEvent.OnPrevDate -> {
                val prevDate = currentState.date.minusDays(1)
                loadSalesForDate(prevDate)
            }

            is MilkSaleListUiEvent.OnDateSelected -> {
                loadSalesForDate(event.date)
            }

            MilkSaleListUiEvent.OnDateClick -> emitEffect(OnDateClick) // Show Picker

            // --- Actions ---
            MilkSaleListUiEvent.OnAddSaleClicked -> {
                // Current Date pass kar rahe hain taake Form me auto-select ho
                val dateMillis = currentState.date.toMillis()
                emitEffect(NavigateToAddSale(dateMillis))
            }

            is MilkSaleListUiEvent.OnEditSaleClicked -> {
                emitEffect(
                    NavigateToEditSale(event.saleId)
                )
            }

            is MilkSaleListUiEvent.OnCustomerDetailClicked -> {
                emitEffect(NavigateToCustomerLedger(event.customerId, event.customerName))
            }


            is MilkSaleListUiEvent.OnBalanceClick -> {
                emitEffect(
                    MilkSaleListUiEffect.OpenBalanceHistorySheet(
                        event.customerId,
                        event.customerName
                    )
                )
            }
        }
    }


    private fun loadSalesForDate(date: LocalDate) {
        updateState { it.copy(isLoading = true, date = date) }
        dateFlow.value = date
    }
}

