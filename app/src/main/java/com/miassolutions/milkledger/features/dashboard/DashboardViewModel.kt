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
        >(
    DashboardUiState(
        workingDate = savedStateHandle["workingDate"] ?: LocalDate.now()
    )
) {


    override fun onEvent(event: DashboardUiEvent) {
        when (event) {

            // 🔵 Date / Range change (Dashboard stats)
            is DashboardUiEvent.OnDateFilterChanged -> {
                updateState {
                    it.copy(
                        isLoading = true,
                        reportStartDate = event.startDate,
                        reportEndDate = event.endDate,

                    )
                }

                loadDashboardData(
                    start = event.startDate,
                    end = event.endDate
                )
            }

            // 🟢 Navigation clicks (forms open with workingDate)
            DashboardUiEvent.OnPurchaseClicked -> {
                emitEffect(
                    NavigateToPurchase(
                        uiState.value.workingDate
                    )
                )
            }

            DashboardUiEvent.OnSaleClicked -> {
                emitEffect(
                    NavigateToSale(
                        uiState.value.workingDate
                    )
                )
            }

            DashboardUiEvent.OnExpenseClicked -> {
                emitEffect(
                    NavigateToExpense(
                        uiState.value.workingDate
                    )
                )
            }

            DashboardUiEvent.OnWalletClicked -> {
                emitEffect(
                    NavigateToWallet(
                        uiState.value.workingDate
                    )
                )
            }

            DashboardUiEvent.OnCashFlowClicked -> {
                emitEffect(
                    NavigateToCashFlow(
                        uiState.value.workingDate
                    )
                )
            }

            DashboardUiEvent.OnNotesClicked -> {
                emitEffect(DashboardUiEffect.NavigateToNotes)
            }

            is DashboardUiEvent.OnWorkingDateChanged -> {
                savedStateHandle["workingDate"] = event.date
                updateState { it.copy(workingDate = event.date) }
            }
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
                        workingDate = it.workingDate, // preserve
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
