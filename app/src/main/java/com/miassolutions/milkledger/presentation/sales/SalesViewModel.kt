package com.miassolutions.milkledger.presentation.sales

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity
import com.miassolutions.milkledger.data.repositories.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        loadSalesForDate(_uiState.value.currentDate)
    }

    fun insertOrUpdateSale(entry: SalesEntryEntity) {
        viewModelScope.launch {
            repository.insertSale(entry)
        }
    }



    fun onEvent(event: SalesUiEvent) {
        when (event) {
            is SalesUiEvent.OnCustomerSelected -> {
                _uiState.update {
                    it.copy(
                        selectedCustomerId = event.supplierId,
                        navToLedgerForCustomerId = event.supplierId
                    )
                }
            }

            is SalesUiEvent.OnVolumeChanged -> updateSaleField(event.entryId) { sale ->
                sale.copy(volume = event.volume)
            }

            is SalesUiEvent.OnDeductionChanged -> updateSaleField(event.entryId) { sale ->
                sale.copy(deduction = event.deduction)
            }

            is SalesUiEvent.OnPaidChanged -> updateSaleField(event.entryId) { sale ->
                sale.copy(paid = event.paid)
            }

            is SalesUiEvent.OnNotesChanged -> updateSaleField(event.entryId) { sale ->
                sale.copy(notes = event.notes)
            }
        }
    }

    fun loadSalesForDate(date: LocalDate) {
        // Update UI state with the selected date immediately
        _uiState.update { it.copy(currentDate = date) }

        viewModelScope.launch {
            // Step 1: Get all customers (sorted if needed)
            val allCustomers = repository.getAllCustomers().first()

            // Step 2: Get existing sales for this date (once)
            val existingSales = repository.getSalesByDateOnce(date)

            // Step 3: Find missing customers with no sales on this date
            val missingCustomers = allCustomers.filterNot { customer ->
                existingSales.any { it.customer.customerId == customer.customerId }
            }

            // Step 4: Insert zeroed sales entries for missing customers
            missingCustomers.forEach { customer ->
                val newSaleEntry = SalesEntryEntity(
                    customerId = customer.customerId,
                    date = date,
                    volume = 0.0,
                    deduction = 0.0,
                    netMilk = 0.0,
                    price = 0.0,
                    paid = 0.0,
                    balance = 0.0,
                    rateUsed = 0.0,
                    notes = ""
                )
                repository.insertSale(newSaleEntry)
            }

            // Step 5: Collect and update UI with sales for date after insertion
            repository.getSalesByDate(date).collect { salesList ->
                // Optionally sort sales by customer name or some order property
                val sortedSales = salesList.sortedBy { it.customer.sortOrder } // example sort

                val totalMilk = sortedSales.sumOf { it.sale.volume }
                val totalDeduction = sortedSales.sumOf { it.sale.deduction }
                val totalAmount = sortedSales.sumOf { it.sale.price }
                val avgRatePerLiter = if (totalMilk > 0) totalAmount / totalMilk else 0.0

                _uiState.update {
                    it.copy(
                        salesForDate = sortedSales,
                        totalMilk = totalMilk,
                        totalDeduction = totalDeduction,
                        totalAmount = totalAmount,
                        grandSaleTotalForDate = totalAmount,
                        avgRatePerLiter = avgRatePerLiter
                    )
                }
            }
        }
    }



    private fun updateSaleField(
        entryId: String,
        transform: (SalesEntryEntity) -> SalesEntryEntity
    ) {
        viewModelScope.launch {
            val currentSale = _uiState.value.salesForDate.find { it.sale.saleId == entryId }?.sale
            if (currentSale != null) {
                val updated = transform(currentSale)
                repository.insertSale(updated)
            }
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navToLedgerForCustomerId = null) }
    }
}
