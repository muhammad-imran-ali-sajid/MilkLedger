package com.miassolutions.milkledger.features.owner.dasboard


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.owner.dasboard.OwnerUiEffect.*
import com.miassolutions.milkledger.features.owner.data.OwnerRepository
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OwnerViewModel @Inject constructor(
    private val repository: OwnerRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<OwnerUiState, OwnerUiEvent, OwnerUiEffect>(OwnerUiState()) {

    private var dashboardJob : Job? = null

    init {
// 🟢 1. Handle Incoming Date Argument
        val argDateMillis: Long = savedStateHandle["date"] ?: -1L

        if (argDateMillis != -1L) {
            val date = argDateMillis.toLocalDate()

            // Initial State update karein
            updateState { it.copy(selectedDate = date) }
        }


    }

    private fun loadDashboardData() {
        dashboardJob?.cancel()
        // ViewModel state se start/end date lein (Jo filter se set hogi)
        val start = currentState.startDate
        val end = currentState.endDate

        updateState { it.copy(isLoading = true) }

        repository.getDashboardData(start, end)
            .onEach { data ->
                updateState {
                    it.copy(
                        isLoading = false,
                        dashboardData = data
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: OwnerUiEvent) {
        when (event) {
            // --- Date Filter ---
            is OwnerUiEvent.OnDateFilterChanged -> {
                updateState {
                    it.copy(
                        startDate = event.start,
                        endDate = event.end,
                        dateLabel = event.label,
                        // 🔥 State Save karein
                        selectedDate = event.selectedDate,
                        filterMode = event.mode
                    )
                }
                loadDashboardData()
            }

            // --- Withdrawal Actions ---
            OwnerUiEvent.OnWithdrawClicked -> {
                // Open Bottom Sheet
                emitEffect(OpenWithdrawSheet(currentState.dashboardData.retainedEarnings))
            }

            is OwnerUiEvent.OnConfirmWithdrawal -> {
                saveWithdrawal(event.id, event.amount, event.date, event.note)
            }

            // --- Navigation ---
            OwnerUiEvent.OnAddExpenseClicked -> emitEffect(NavigateToAddExpense)
            is OwnerUiEvent.OnDeleteWithdrawal -> {
                performDelete(event.id)
                emitEffect(ShowSnackbar("Withdrawal deleted"))
            }

            OwnerUiEvent.OnNetProfitClicked -> {
                loadProfitBreakdown()
            }
        }
    }


    private fun loadProfitBreakdown() {
        val start = currentState.startDate
        val end = currentState.endDate

        viewModelScope.launch {
            // Hum One-Time collect karenge kyunke ye "Report" hai
            // (Ya Flow collect kr k bhi bhej skte hen)
            repository.getProfitBreakdown(start, end)
                .collect { list ->
                    // List UI ko bhej den
                    emitEffect(OwnerUiEffect.OpenProfitDetailsSheet(list))
                }
        }
    }

    private fun saveWithdrawal(id: String?, amountStr: String, date: LocalDate, note: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            emitEffect(OwnerUiEffect.ShowSnackbar("Please enter valid amount"))
            return
        }

        // 🔥 FIX: Yahan check karein. Agar note blank hai to "Withdraw" set karein
        val finalNote = note.ifBlank { "Withdraw" }

        viewModelScope.launch {
            try {
                // Convert to Paisa (Long)
                val amountPaisa = (amount * 100).toLong()

                if (id == null) {
                    // ✅ finalNote use karein
                    repository.saveCashWithdrawal(amountPaisa, date, finalNote)
                    emitEffect(OwnerUiEffect.ShowSnackbar("Withdrawal Successful"))
                } else {
                    // ✅ finalNote use karein
                    repository.updateCashWithdrawal(id, amountPaisa, date, finalNote)
                    emitEffect(OwnerUiEffect.ShowSnackbar("Withdrawal updated successful"))
                }

                emitEffect(OwnerUiEffect.CloseWithdrawSheet)

            } catch (e: Exception) {
                emitEffect(OwnerUiEffect.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }


    private fun performDelete(id: String) {
        viewModelScope.launch {
            try {
                // 1. DB se delete karein
                repository.deleteTransaction(id)

                // 2. Success Message
                emitEffect(OwnerUiEffect.ShowSnackbar("Withdrawal deleted"))

                // 3. 🔥 FIX: Sheet Band Karein (Ye line missing thi)
                emitEffect(OwnerUiEffect.CloseWithdrawSheet)

            } catch (e: Exception) {
                // 4. 🔥 FIX: Error Chupayen nahi, User ko batayen
                emitEffect(OwnerUiEffect.ShowSnackbar("Delete Error: ${e.message}"))
            }
        }
    }
}