package com.miassolutions.milkledger.features.owner.dasboard


import com.miassolutions.milkledger.features.owner.domain.OwnerDashboardData
import java.time.LocalDate

data class OwnerUiState(
    val isLoading: Boolean = false,

    // Date Filter State
    val startDate: Long = 0L, // Default should be start of month logic in VM
    val endDate: Long = Long.MAX_VALUE,
    val dateLabel: String = "All History",

    // Main Data
    // Default empty object
    val dashboardData: OwnerDashboardData = OwnerDashboardData(0, 0, emptyList())
)

sealed class OwnerUiEvent {
    // Dashboard Events
    data class OnDateFilterChanged(val start: Long, val end: Long, val label: String) : OwnerUiEvent()
    object OnWithdrawClicked : OwnerUiEvent()
    object OnAddExpenseClicked : OwnerUiEvent()

    // Withdraw Sheet Events
    data class OnConfirmWithdrawal( val amount: String, val date: LocalDate, val note: String) : OwnerUiEvent()
}

sealed class OwnerUiEffect {
    data class ShowSnackbar(val message: String) : OwnerUiEffect()
    object NavigateToAddExpense : OwnerUiEffect()

    // Sheet Control
    data class OpenWithdrawSheet(val availableBalance: Long) : OwnerUiEffect()
    object CloseWithdrawSheet : OwnerUiEffect()
}