package com.miassolutions.milkledger.features.dashboard

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.backup.data.BackupPrefs
import com.miassolutions.milkledger.features.dashboard.DashboardUiEffect.NavigateToExpense
import com.miassolutions.milkledger.features.dashboard.DashboardUiEffect.NavigateToPurchase
import com.miassolutions.milkledger.features.dashboard.DashboardUiEffect.NavigateToSale
import com.miassolutions.milkledger.features.dashboard.data.DashboardRepository
import com.miassolutions.milkledger.utils.customview.DateFilterView
import com.miassolutions.milkledger.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
   private val backupPrefs: BackupPrefs,
    private val repository: DashboardRepository,
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
                        selectedDate = event.selectedSingleDate,
                        filterMode = event.mode

                    )
                }

                loadDashboardData(
                    start = event.startDate,
                    end = event.endDate
                )
            }

            // 🟢 Navigation clicks (forms open with workingDate)
            DashboardUiEvent.OnPurchaseClicked -> {
                emitEffect(NavigateToPurchase(getDateForNavigation()))
            }

            DashboardUiEvent.OnSaleClicked -> {
                emitEffect(NavigateToSale(getDateForNavigation()))
            }

            DashboardUiEvent.OnExpenseClicked -> {
                emitEffect(NavigateToExpense(getDateForNavigation()))
            }

        }
    }

    // Ye decide karega k konsi date aage bhejni hai
    private fun getDateForNavigation(): LocalDate {
        val state = uiState.value

        return if (state.filterMode == DateFilterView.FilterMode.DAY) {
            // Agar DAY mode hai, to jo date user dekh raha hai wahi pass karo
            state.selectedDate
        } else {
            // Agar Month/Year/Custom hai, to 'AAJ' ki date pass karo
            // Taake user ghalti se purani entry na kar de
            LocalDate.now()
        }
    }

    // 🔵 Dashboard stats loader (report range only)
    private fun loadDashboardData(start: LocalDate, end: LocalDate) {
        val status = backupPrefs.getBackupStatusSnapshot()
        updateState { state ->
            state.copy(lastSuccessfulBackupAt = status.lastSuccessfulBackupAt)
        }

        repository.getDashboardData(start.toMillis(), end.toMillis())
            .onEach { dashboardState ->


                updateState { currentState ->
                    dashboardState.copy(
                        reportStartDate = start,
                        reportEndDate = end,
                        isLoading = false,

                        // ✅ FIX 1: Date preserve karein
                        selectedDate = currentState.selectedDate,

                        // ✅ FIX 2: Filter Mode bhi preserve karein (Warna ye default DAY ho jayega)
                        filterMode = currentState.filterMode,

                        )
                }
            }
            .launchIn(viewModelScope)
    }
}
