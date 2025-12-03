package com.miassolutions.milkledger.presentation.customer.sales

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesSummary
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.mapper.toSalesList
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.data.repository.SalesRepository
import com.miassolutions.milkledger.presentation.stats.CustomerPaidSummary
import com.miassolutions.milkledger.presentation.supplier.BalanceHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository,
    private val purchaseRepo: PurchaseRepository
) : ViewModel() {

    // -------------------------------------------------------------------------
    // UI State — Clean Like PurchaseUiState
    // -------------------------------------------------------------------------
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    // -------------------------------------------------------------------------
    // Paid sales for the selected date
    // -------------------------------------------------------------------------
    private val _paidSalesList = MutableStateFlow<List<CustomerPaidSummary>>(emptyList())
    val paidSalesList: StateFlow<List<CustomerPaidSummary>> = _paidSalesList.asStateFlow()

    // -------------------------------------------------------------------------
    // Balance History
    // -------------------------------------------------------------------------
    private val _balanceHistory = MutableStateFlow<List<BalanceHistory>>(emptyList())
    val balanceHistory: StateFlow<List<BalanceHistory>> = _balanceHistory.asStateFlow()

    // -------------------------------------------------------------------------
    // Init — Same pattern as PurchaseViewModel
    // -------------------------------------------------------------------------
    init {
        observeSalesForDate(_uiState.value.currentDate)
        observePaidSales(_uiState.value.currentDate)
    }

    // -------------------------------------------------------------------------
    // Load Balance History
    // -------------------------------------------------------------------------
    fun loadBalanceHistory(customerId: String) = viewModelScope.launch {
        val list = repository.getBalanceHistory(customerId)
        _balanceHistory.value = list
        Log.d("SalesVM", "Balance history loaded for: $customerId size=${list.size}")
    }

    // -------------------------------------------------------------------------
    // Observe paid sales for specific date
    // -------------------------------------------------------------------------
    private fun observePaidSales(date: LocalDate) {
        repository.getPaidSalesForDate(date)
            .onEach { list -> _paidSalesList.value = list }
            .catch { Log.e("SalesVM", "Paid sales error: $it") }
            .launchIn(viewModelScope)
    }

    // -------------------------------------------------------------------------
    // Update sale manually (same pattern as updatePurchaseManually)
    // -------------------------------------------------------------------------
    fun updateSaleManually(updated: SalesEntity) {
        viewModelScope.launch {
            val newPrice = MilkCalculationUtils.calculateCustomerPrice(
                volume = updated.volume,
                deduction = updated.deduction,
                rate = updated.rateUsed
            )

            val netMilk = updated.volume - updated.deduction

            val final = updated.copy(
                price = newPrice,
                netMilk = netMilk
            )

            repository.updateSale(final)
        }
    }

    // -------------------------------------------------------------------------
    // Handle Date Selection (Same logic as PurchaseViewModel.onDateSelected)
    // -------------------------------------------------------------------------
    fun onDateSelected(date: LocalDate) {
        if (date != _uiState.value.currentDate) {
            _uiState.update { it.copy(currentDate = date) }
            observeSalesForDate(date)
            observePaidSales(date)
        }
    }

    // -------------------------------------------------------------------------
    // Clean Observe Logic — SAME STYLE AS PurchaseViewModel.observeForDate()
    // -------------------------------------------------------------------------
    private var salesJob: Job? = null

    private fun observeSalesForDate(date: LocalDate) {
        salesJob?.cancel()
        salesJob = viewModelScope.launch {

            // Set loading
            _uiState.update { it.copy(isLoading = true) }

            // Ensure all customers have an entry
            ensureSalesEntriesExist(date)

            repository.getSalesByDate(date).collectLatest { list ->

                val sorted = list.sortedBy { it.customer.sortOrder }
                val salesList = sorted.map { it.toSalesList() }

                val totalVolume = salesList.sumOf { it.volume }
                val totalDeduction = salesList.sumOf { it.deduction }
                val totalNet = salesList.sumOf { it.netVolume }
                val totalPrice = salesList.sumOf { it.price }
                val received = salesList.sumOf { it.received }
                val totalBalance = salesList.sumOf { it.price - it.received }

                val purchaseTotalVolume =
                    purchaseRepo.getPurchasesByDateOnce(date).sumOf { it.purchase.milkAmount }

                val avgRate =
                    if (purchaseTotalVolume > 0) totalPrice / purchaseTotalVolume else 0.0

                _uiState.update {
                    it.copy(
                        currentDate = date,
                        salesForDate = salesList,
                        totalMilk = totalVolume,
                        totalDeduction = totalDeduction,
                        totalNetMilk = totalNet,
                        grandSaleTotalForDate = totalPrice,
                        receivedAmount = received,
                        totalBalance = totalBalance,
                        avgRatePerLiter = avgRate,
                        pdfSalesSummary = PdfSalesSummary(
                            totalQty = totalVolume.toRoundedStr(),
                            totalDeduction = totalDeduction.toRoundedStr(),
                            totalAmount = totalPrice.toPriceStr(),
                            totalPaid = received.toPriceStr(),
                            balanceDue = totalBalance.toPriceStr()
                        ),
                        isLoading = false
                    )
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Ensure each customer has an entry — SAME STYLE AS Purchase Ensure Logic
    // -------------------------------------------------------------------------
    private suspend fun ensureSalesEntriesExist(date: LocalDate) {
        val allCustomers = repository.getAllCustomers().first()
        val existing = repository.getSalesByDateOnce(date)

        val missing = allCustomers.filterNot { customer ->
            existing.any { it.customer.customerId == customer.customerId }
        }

        missing.forEach { c ->
            val sale = SalesEntity(
                saleId = "${c.customerId}_$date",
                customerId = c.customerId,
                date = date,
                volume = 0.0,
                deduction = 0.0,
                netMilk = 0.0,
                price = 0.0,
                paid = 0.0,
                rateUsed = c.customerRate,
                balance = 0.0
            )
            repository.insertSale(sale)
        }
    }



    // -------------------------------------------------------------------------
    // Handle UI Events — Same Style As Purchase
    // -------------------------------------------------------------------------
    fun onEvent(event: SalesUiEvent) {
        when (event) {
            is SalesUiEvent.OnCustomerSelected ->
                _uiState.update { it.copy(navToLedgerForCustomerId = event.supplierId) }

            is SalesUiEvent.SelectDate ->
                onDateSelected(event.date)
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navToLedgerForCustomerId = null) }
    }
}
