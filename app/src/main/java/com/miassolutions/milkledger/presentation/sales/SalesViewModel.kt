package com.miassolutions.milkledger.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.repositories.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    // ----------------------------------------------------------
    // 🌟 UI State
    // ----------------------------------------------------------
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    // ----------------------------------------------------------
    // ⚙️ Initialize with today's sales
    // ----------------------------------------------------------
    init {
        observeForDate(_uiState.value.currentDate)
    }

    // ----------------------------------------------------------
    // 🧮 Update Sale Entry Manually
    // ----------------------------------------------------------
    fun updateSaleManually(updated: SalesEntryEntity) {
        viewModelScope.launch {
            // Recalculate price based on milk parameters
            val newPrice = MilkCalculationUtils.calculateCustomerPrice(
                rate = updated.rateUsed,
                volume = updated.volume,
                deduction = updated.deduction,
            )

            val finalEntry = updated.copy(price = newPrice)
            repository.updateSale(finalEntry)
        }
    }

    // ----------------------------------------------------------
    // 📅 When date changes
    // ----------------------------------------------------------
    fun onDateSelected(date: LocalDate) {
        if (date != _uiState.value.currentDate) {
            observeForDate(date)
        }
    }

    // ----------------------------------------------------------
    // 🔁 Observe sales for selected date
    // ----------------------------------------------------------
    private var salesJob: Job? = null

    fun observeForDate(date: LocalDate) {
        salesJob?.cancel()
        salesJob = viewModelScope.launch {
            val allCustomers = repository.getAllCustomers().first()
            val existingSales = repository.getSalesByDateOnce(date)

            // Auto-create empty entries for customers missing for that date
            val missingCustomers = allCustomers.filterNot { customer ->
                existingSales.any { it.customer.customerId == customer.customerId }
            }

            missingCustomers.forEach { customer ->
                val newSale = SalesEntryEntity(
                    customerId = customer.customerId,
                    date = date,
                    volume = 0.0,
                    deduction = 0.0,
                    netMilk = 0.0,
                    price = 0.0,
                    paid = 0.0,
                    rateUsed = customer.customerRate,
                    balance = 0.0

                    )
                repository.insertSale(newSale)
            }

            // Observe sales in realtime
            repository.getSalesByDate(date).collectLatest { sales ->
                val sortedSales = sales.sortedBy { it.customer.sortOrder }

                val totalMilk = sortedSales.sumOf { it.sale.volume }
                val totalDeduction = sortedSales.sumOf { it.sale.deduction }
                val totalAmount = sortedSales.sumOf { it.sale.price - it.sale.deduction }
                val grandTotal = sortedSales.sumOf { it.sale.price }
                val avgRatePerLiter =
                    if (totalMilk > 0) grandTotal / totalMilk else 0.0

                _uiState.update {
                    it.copy(
                        currentDate = date,
                        salesForDate = sortedSales,
                        totalMilk = totalMilk,
                        totalDeduction = totalDeduction,
                        totalAmount = totalAmount,
                        grandSaleTotalForDate = grandTotal,
                        avgRatePerLiter = avgRatePerLiter
                    )
                }
            }
        }
    }

    // ----------------------------------------------------------
    // ⚡ Handle UI Events
    // ----------------------------------------------------------
    fun onEvent(event: SalesUiEvent) {
        when (event) {
            is SalesUiEvent.OnCustomerSelected ->
                _uiState.update { it.copy(navToLedgerForCustomerId = event.supplierId) }
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navToLedgerForCustomerId = null) }
    }
}
