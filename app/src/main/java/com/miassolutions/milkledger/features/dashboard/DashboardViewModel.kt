package com.miassolutions.milkledger.features.dashboard




import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.dashboard.data.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : BaseViewModel<DashboardUiState, DashboardUiEvent, DashboardUiEffect>(DashboardUiState()) {

    // Init block ki zaroorat nahi, kyunke DateFilterView khud start me date set karega

    override fun onEvent(event: DashboardUiEvent) {
        when(event) {
            is DashboardUiEvent.OnDateFilterChanged -> {
                loadDashboardData(event.start, event.end)
            }
            DashboardUiEvent.OnCashFlowClicked -> {
                emitEffect(DashboardUiEffect.NavigateToCashFlow)
            }
            DashboardUiEvent.OnNotesClicked -> {
                emitEffect(DashboardUiEffect.NavigateToNotes)
            }
        }
    }

    private fun loadDashboardData(start: Long, end: Long) {
        updateState { it.copy(isLoading = true) }

        repository.getDashboardData(start, end)
            .onEach { data ->
                // Repository se jo calculated state mili, usay update kr den
                updateState { data }
            }
            .launchIn(viewModelScope)
    }
}