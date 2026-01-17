package com.miassolutions.milkledger.features.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.dashboard.DashboardUiEffect.*
import com.miassolutions.milkledger.features.dashboard.data.DashboardRepository
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val savedStateHandle: SavedStateHandle
) : BaseViewModel<
        DashboardUiState,
        DashboardUiEvent,
        DashboardUiEffect
        >(DashboardUiState()) {


    override fun onEvent(event: DashboardUiEvent) {
        when (event) {

            // 🔵 Date / Range change (Dashboard stats)
            is DashboardUiEvent.OnDateFilterChanged -> {
                updateState {
                    it.copy(
                        isLoading = true,
                        reportStartDate = event.startDate,
                        reportEndDate = event.endDate,
                        selectedDate = event.selectedSingleDate

                    )
                }

                loadDashboardData(
                    start = event.startDate,
                    end = event.endDate
                )
            }

            // 🟢 Navigation clicks (forms open with workingDate)
            DashboardUiEvent.OnPurchaseClicked -> {
                emitEffect(NavigateToPurchase(uiState.value.selectedDate))
            }

            DashboardUiEvent.OnSaleClicked -> {
                emitEffect(NavigateToSale(uiState.value.selectedDate))
            }

            DashboardUiEvent.OnExpenseClicked -> {
                emitEffect(NavigateToExpense(uiState.value.selectedDate))
            }

            DashboardUiEvent.OnWalletClicked -> {
                emitEffect(NavigateToWallet(uiState.value.selectedDate))
            }

            DashboardUiEvent.OnCashFlowClicked -> {
                emitEffect(NavigateToCashFlow(uiState.value.selectedDate))
            }

            DashboardUiEvent.OnNotesClicked -> emitEffect(DashboardUiEffect.NavigateToNotes)


        }
    }

    // 🔵 Dashboard stats loader (report range only)
    private fun loadDashboardData(
        start: LocalDate,
        end: LocalDate
    ) {
        repository.getDashboardData(
            start.toMillis(),
            end.toMillis()
        )
            .onEach { dashboardState ->
                // Repository already calculated data return kar raha hai
                updateState {
                    dashboardState.copy(
                        reportStartDate = start,
                        reportEndDate = end,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
