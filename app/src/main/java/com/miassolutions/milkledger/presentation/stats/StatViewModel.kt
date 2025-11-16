package com.miassolutions.milkledger.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    // 1. Define the input date stream (can be changed dynamically)
    private val _targetDateFlow = MutableStateFlow(LocalDate.now().minusDays(1))
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

    // --- Combine Streams into UI State ---

    val dashboardState: StateFlow<StatDashboardState> =
        combine(
            _targetDateFlow,
            customerPaymentsFlow,
            supplierPaymentsFlow
        ) { date, customerList, supplierList ->
            // 2. Map the three combined results into the final State
            StatDashboardState(
                targetDate = date,
                customerPayments = customerList,
                supplierPayments = supplierList,
                isLoading = false // Data has loaded
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
                // Return an empty list or a loading indicator item
                emptyList()
            } else {
                // Build the final list with headers and items
                buildList {
                    // Customer Section
                    add(StatListItem.Header("Customer Payments"))
                    state.customerPayments.map { summary ->
                        add(StatListItem.CustomerItem(summary))
                    }

                    // Supplier Section
                    add(StatListItem.Header("Supplier Payments"))
                    state.supplierPayments.map { summary ->
                        add(StatListItem.SupplierItem(summary))
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