package com.miassolutions.milkledger.presentation.stats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.data.repository.ExpensesRepository
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.data.repository.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class StatViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val purchaseRepository: PurchaseRepository,
    private val expensesRepository: ExpensesRepository

) : ViewModel() {

    // 1. Define the input date stream (can be changed dynamically)
    private val _targetDateFlow = MutableStateFlow(LocalDate.now())
    val targetDate: StateFlow<LocalDate> = _targetDateFlow

    // --- Data Streams from Repositories ---


    // Flow for customer payments on the target date

    private val customerPaymentsFlow: Flow<List<CustomerPaidSummary>> =
        _targetDateFlow.flatMapLatest { date ->
            salesRepository.getPaidSalesForDate(date)
        }

    // Flow for supplier payments on the target date
    private val supplierPaymentsFlow: Flow<List<SupplierPaidSummary>> =
        _targetDateFlow.flatMapLatest { date ->
            purchaseRepository.getPaidToSuppliersForDate(date)
        }

    private val totalExpenseFlow: Flow<List<ExpenseSummary>> =
        _targetDateFlow.flatMapLatest { date ->
            expensesRepository.getTotalExpenses(date)
        }

    // --- Combine Streams into UI State ---

    val dashboardState: StateFlow<StatDashboardState> =
        combine(
            _targetDateFlow,
            customerPaymentsFlow,
            supplierPaymentsFlow,
            totalExpenseFlow
        ) { date, customerList, supplierList, expenseList ->
            // 2. Map the three combined results into the final State
            val totalCustomerPayment = customerList.sumOf { it.paidAmount }
            val totalSupplierPayment = supplierList.sumOf { it.paidAmount }
            val totalExpense = expenseList.sumOf { it.expenseAmount }


            Log.d("StatsViewModel", "$expenseList - $totalCustomerPayment")

            StatDashboardState(

                targetDate = date,
                customerPayments = customerList,
                supplierPayments = supplierList,
                expenseList = expenseList,
                isLoading = false,
                totalCustomerPayment = totalCustomerPayment,
                totalSupplierPayment = totalSupplierPayment,
                totalExpenses = totalExpense
            )
        }
            // 3. Convert the Flow into a StateFlow to hold the latest value
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = StatDashboardState(isLoading = true) // Initial loading state
            )


    val combinedList: Flow<List<StatListItem>> = dashboardState
        .map { state ->
            if (state.isLoading) {
                emptyList()
            } else {
                // Build the final list with headers, items, and totals
                buildList {
                    // --- Customer Section ---
                    add(StatListItem.Header("Customer Payments"))
                    state.customerPayments.map { summary ->
                        add(StatListItem.CustomerItem(summary))
                    }
                    // Add Customer Total Summary
                    if (state.totalCustomerPayment > 0 || state.customerPayments.isNotEmpty()) {
                        add(StatListItem.TotalSummary("TOTAL RECEIVED", state.totalCustomerPayment))
                    }


                    // --- Supplier Section ---
                    add(StatListItem.Header("Supplier Payments"))
                    state.supplierPayments.map { summary ->
                        add(StatListItem.SupplierItem(summary))
                    }
                    // Add Supplier Total Summary
                    if (state.totalSupplierPayment > 0 || state.supplierPayments.isNotEmpty()) {
                        add(StatListItem.TotalSummary("TOTAL PAID", state.totalSupplierPayment))
                    }

                    // --- Expenses Section ---
                    add(StatListItem.Header("Expenses"))
                    state.expenseList.map { summary ->
                        add(StatListItem.ExpenseItem(summary))
                    }
                    // Add Expense Total Summary
                    if (state.totalExpenses > 0 || state.expenseList.isNotEmpty()) {
                        add(StatListItem.TotalSummary("TOTAL EXPENSES", state.totalExpenses))
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Function to update the date, which automatically triggers a re-query
     * for both customer and supplier data streams.
     */
    fun setTargetDate(newDate: LocalDate) {
        if (newDate != _targetDateFlow.value) {
            _targetDateFlow.value = newDate
        }
    }
}