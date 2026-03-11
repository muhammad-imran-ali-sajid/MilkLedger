package com.miassolutions.milkledger.features.owner.dasboard


import com.miassolutions.milkledger.features.owner.domain.DailyProfitTuple
import com.miassolutions.milkledger.features.owner.domain.OwnerDashboardData
import com.miassolutions.milkledger.utils.customview.DateFilterView
import java.time.LocalDate

data class OwnerUiState(
    val isLoading: Boolean = false,

    // Date Filter State
    val startDate: Long = 0L, // Default should be start of month logic in VM
    val endDate: Long = Long.MAX_VALUE,
    val dateLabel: String = "All History",

    // 🔥 FOR STATE RESTORATION
    val selectedDate: LocalDate = LocalDate.now(),
    val filterMode: DateFilterView.FilterMode = DateFilterView.FilterMode.DAY,


    // Main Data
    // Default empty object
    val dashboardData: OwnerDashboardData = OwnerDashboardData(0, 0, 0, emptyList())
)

sealed class OwnerUiEvent {
    // Dashboard Events
    data class OnDateFilterChanged(
        val start: Long,
        val end: Long,
        val label: String,
        val selectedDate: LocalDate,
        val mode: DateFilterView.FilterMode
    ) :
        OwnerUiEvent()

    object OnWithdrawClicked : OwnerUiEvent()
    object OnAddExpenseClicked : OwnerUiEvent()

    object OnNetProfitClicked : OwnerUiEvent()
    data class OnDeleteWithdrawal(val id: String) : OwnerUiEvent()

    // Withdraw Sheet Events
    data class OnConfirmWithdrawal(
        val id: String? = null, // 🔥 New Field
        val amount: String,
        val date: LocalDate,
        val note: String
    ) : OwnerUiEvent()
}

sealed class OwnerUiEffect {
    data class ShowSnackbar(val message: String) : OwnerUiEffect()
    object NavigateToAddExpense : OwnerUiEffect()


    data class OpenProfitDetailsSheet(val data: List<DailyProfitTuple>) : OwnerUiEffect()


    // Sheet Control
    data class OpenWithdrawSheet(val availableBalance: Long) : OwnerUiEffect()
    object CloseWithdrawSheet : OwnerUiEffect()
}