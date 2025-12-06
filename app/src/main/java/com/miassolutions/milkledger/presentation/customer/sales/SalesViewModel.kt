package com.miassolutions.milkledger.presentation.customer.sales

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesSummary
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
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
    // UI State — like PurchaseUiState
    // -------------------------------------------------------------------------
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()



    // -------------------------------------------------------------------------
    // Paid sales for selected date
    // -------------------------------------------------------------------------
    private val _paidSalesList = MutableStateFlow<List<CustomerPaidSummary>>(emptyList())
    val paidSalesList: StateFlow<List<CustomerPaidSummary>> = _paidSalesList.asStateFlow()

    // -------------------------------------------------------------------------
    // Balance History
    // -------------------------------------------------------------------------
    private val _balanceHistory = MutableStateFlow<List<BalanceHistory>>(emptyList())
    val balanceHistory: StateFlow<List<BalanceHistory>> = _balanceHistory.asStateFlow()

    // -------------------------------------------------------------------------
    // Init
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
    // Observe paid sales
    // -------------------------------------------------------------------------
    private fun observePaidSales(date: LocalDate) {
        repository.getPaidSalesForDate(date)
            .onEach { list -> _paidSalesList.value = list }
            .catch { Log.e("SalesVM", "Paid sales error: $it") }
            .launchIn(viewModelScope)
    }

    // -------------------------------------------------------------------------
    // Update sale manually
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
    // Date changed
    // -------------------------------------------------------------------------
    fun onDateSelected(date: LocalDate) {
        if (date != _uiState.value.currentDate) {
            _uiState.update { it.copy(currentDate = date) }
            observeSalesForDate(date)
            observePaidSales(date)
        }
    }

    fun goToNextDate() {
        val next = _uiState.value.currentDate.plusDays(1)
        onDateSelected(next)
    }

    fun goToPreviousDate() {
        val prev = _uiState.value.currentDate.minusDays(1)
        onDateSelected(prev)
    }

    // -------------------------------------------------------------------------
    // Observe sales for date (CLEAN — NO auto-generation logic)
    // -------------------------------------------------------------------------
    private var salesJob: Job? = null

    private fun observeSalesForDate(date: LocalDate) {
        salesJob?.cancel()
        salesJob = viewModelScope.launch {

            _uiState.update { it.copy(isLoading = true) }

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

    fun deleteSale(saleId: String){
        viewModelScope.launch {
            repository.deleteSale(saleId)
        }
    }

    // -------------------------------------------------------------------------
    // Handle UI Events
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
