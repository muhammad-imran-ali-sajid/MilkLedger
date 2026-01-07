package com.miassolutions.milkledger.features.sale.ui.list

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEffect
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEffect.*
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiEvent
import com.miassolutions.milkledger.features.sale.salelist.MilkSaleListUiState
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MilkSaleListViewModel @Inject constructor(
    private val repository: MilkSaleRepository
) : BaseViewModel<MilkSaleListUiState, MilkSaleListUiEvent, MilkSaleListUiEffect>(
    MilkSaleListUiState()
) {

    private var dataJob: Job? = null

    init {
        // App khultay hi Aaj ka data load karein
        loadSalesForDate(LocalDate.now())
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

            is MilkSaleListUiEvent.OnDeleteClicked -> {
                onDeleteSaleClicked(event.saleId)
            }
        }
    }

    fun onDeleteSaleClicked(saleId: String) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }
            try {
                repository.deleteSale(saleId)

                // Success Message
                emitEffect(ShowSnackbar("Sale Deleted Successfully"))

                // List auto-refresh ho jayegi kyunke Flow use ho raha hai

            } catch (e: Exception) {
                emitEffect(ShowSnackbar("Error: ${e.message}"))
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadSalesForDate(date: LocalDate) {
        // 1. State me Date update karein
        updateState { it.copy(date = date, isLoading = true) }

        // 2. Query ke liye Start/End time nikalein
        val startOfDay = date.atStartOfDay().toMillis()
        val endOfDay = date.plusDays(1).atStartOfDay().toMillis() - 1

        // 3. Purani flow cancel karein (Fast switching ke liye)
        dataJob?.cancel()

        // 4. Data fetch karein
        dataJob = repository.getSalesByDate(startOfDay, endOfDay)
            .onEach { list ->
                // Summary Calculation (List aate hi total kar lein)
                val totalMilk = list.sumOf { it.netQuantity }
                val totalAmt = list.sumOf { it.totalAmount }

                updateState {
                    it.copy(
                        isLoading = false,
                        sales = list,
                        totalMilk = totalMilk,
                        totalAmount = totalAmt
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

