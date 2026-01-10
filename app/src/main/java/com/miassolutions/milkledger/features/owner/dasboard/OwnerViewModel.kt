package com.miassolutions.milkledger.features.owner.dasboard


import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.owner.dasboard.OwnerUiEffect.*
import com.miassolutions.milkledger.features.owner.data.OwnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OwnerViewModel @Inject constructor(
    private val repository: OwnerRepository
) : BaseViewModel<OwnerUiState, OwnerUiEvent, OwnerUiEffect>(OwnerUiState()) {

    init {
        // Initial Data Load (Current Month)
        loadDashboardData()
    }

    private fun loadDashboardData() {
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
                        dateLabel = event.label
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
            OwnerUiEvent.OnAddExpenseClicked -> emitEffect(OwnerUiEffect.NavigateToAddExpense)
            is OwnerUiEvent.OnDeleteWithdrawal -> {
                performDelete(event.id)
                emitEffect(OwnerUiEffect.ShowSnackbar("Withdrawal deleted"))
            }
        }
    }

    private fun saveWithdrawal(id: String?, amountStr: String, date: LocalDate, note: String) {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            emitEffect(OwnerUiEffect.ShowSnackbar("Please enter valid amount"))
            return
        }

        viewModelScope.launch {
            try {
                // Convert to Paisa (Long)
                val amountPaisa = (amount * 100).toLong()

                if (id == null) {
                    repository.saveCashWithdrawal(amountPaisa, date, note)
                    emitEffect(OwnerUiEffect.ShowSnackbar("Withdrawal Successful"))
                } else {
                    repository.updateCashWithdrawal(id, amountPaisa, date, note)
                    emitEffect(OwnerUiEffect.ShowSnackbar("Withdrawal updated successful"))
                }


                emitEffect(OwnerUiEffect.CloseWithdrawSheet)

                // Data auto-refresh ho jayega kyunke Flow observe ho raha hai

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