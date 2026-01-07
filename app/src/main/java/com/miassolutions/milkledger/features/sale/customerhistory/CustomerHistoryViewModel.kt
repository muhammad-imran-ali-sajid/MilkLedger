package com.miassolutions.milkledger.features.sale.customerhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CustomerHistoryViewModel @Inject constructor(
    private val repository: MilkSaleRepository,
    savedStateHandle: SavedStateHandle // Agar arguments se ID aani hai
) : BaseViewModel<CustomerHistoryUiState, CustomerHistoryUiEvent, CustomerHistoryUiEffect>(CustomerHistoryUiState()) {

    // Fragment k arguments se ID uthayen
    private val customerId: String = savedStateHandle["customerId"] ?: ""

    init {
        if (customerId.isNotEmpty()) {
            loadHistory()
            loadCurrentBalance()
        }
    }

    // Example inside ViewModel
    private fun loadHistory() {
        repository.getCustomerHistory(customerId)
            .onEach { list ->
                // Summary Calculate karein
                val totalMilk = list.sumOf { it.netQuantity }
                val totalReceived = list.sumOf { it.paymentReceived }

                updateState {
                    it.copy(
                        isLoading = false,
                        transactions = list,
                        summaryMilk = totalMilk,
                        summaryReceived = totalReceived
                        // Balance alag flow se aayega
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadCurrentBalance() {
        // 2. Abhi ka Total Balance (Jo sab se neechay show hoga)
        repository.getCustomerBalance(customerId)
            .onEach { balance ->
                updateState { it.copy(currentTotalBalance = balance) }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: CustomerHistoryUiEvent) {
        TODO("Not yet implemented")
    }

    // ... onEvent handlers
}