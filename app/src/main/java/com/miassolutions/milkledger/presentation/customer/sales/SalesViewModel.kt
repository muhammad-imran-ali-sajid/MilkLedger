package com.miassolutions.milkledger.presentation.customer.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesSummary
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import com.miassolutions.milkledger.data.repository.PurchaseRepository
import com.miassolutions.milkledger.data.repository.SalesRepository
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
    private val repository: SalesRepository,
    private val pRepo : PurchaseRepository
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
        // Start observing for today's date
        observeSalesForDate(_uiState.value.currentDate)
    }

    // ----------------------------------------------------------
    // 🧮 Update Sale Entry Manually
    // ----------------------------------------------------------
    fun updateSaleManually(updated: SalesEntity) {
        viewModelScope.launch {
            // Recalculate price based on milk parameters
            val newPrice = MilkCalculationUtils.calculateCustomerPrice(
                rate = updated.rateUsed,
                volume = updated.volume,
                deduction = updated.deduction,
            )

            // NOTE: The 'deduction' is usually a volume/quantity deduction before pricing,
            // so we calculate netMilk here, which is essential for the calculation utility.
            val netMilk = updated.volume - updated.deduction

            val finalEntry = updated.copy(
                price = newPrice,
                netMilk = netMilk // Ensure netMilk is correctly calculated
            )
            repository.updateSale(finalEntry)
        }
    }

    // ----------------------------------------------------------
    // 📅 When date changes
    // ----------------------------------------------------------
    fun onDateSelected(date: LocalDate) {
        if (date != _uiState.value.currentDate) {
            // 1. Update the UI state date immediately
            _uiState.update { it.copy(currentDate = date) }

            // 2. Start the combined setup and observation logic
            observeSalesForDate(date)
        }
    }

    // ----------------------------------------------------------
    // 🔁 Combined logic for setup (once) and observation (real-time)
    // ----------------------------------------------------------
    private var salesJob: Job? = null

    // Renamed for clarity: observeForDate -> observeSalesForDate
    private fun observeSalesForDate(date: LocalDate) {
        salesJob?.cancel()

        // 1. **CRITICAL FIX:** Perform the initial data setup/insertion OUTSIDE of the Flow collection.
        // This ensures the local write does not immediately trigger the flow again.
        viewModelScope.launch {
            ensureSalesEntriesExist(date)
        }

        // 2. Start the real-time observation job
        salesJob = viewModelScope.launch {
            repository.getSalesByDate(date).collectLatest { sales ->
                val sortedSales = sales.sortedBy { it.customer.sortOrder }


                val totalVolume = sortedSales.sumOf { it.sale.volume }
                val totalDeduction = sortedSales.sumOf { it.sale.deduction }

                val validForAvg = sortedSales.filter { it.customer.customerRate > 0.0 }
                val aTotalPrice = validForAvg.sumOf { it.sale.price }
                val aTotalVolume = validForAvg.sumOf { it.sale.volume }

                // as per original ledger
                val pTotalVolume = pRepo.getPurchasesByDateOnce(date).sumOf { it.purchase.milkAmount }



                val grandTotalPrice = sortedSales.sumOf { it.sale.price }
                val totalNetMilk = sortedSales.sumOf { it.sale.netMilk }
                val totalBalance = sortedSales.sumOf { it.sale.price - it.sale.paid }

                val avgRatePerLiter =
                    if (aTotalVolume > 0) aTotalPrice / pTotalVolume else 0.0

                _uiState.update {
                    it.copy(
                        currentDate = date,
                        salesForDate = sortedSales,
                        totalMilk = totalVolume,
                        totalDeduction = totalDeduction,
                        totalAmount = totalBalance, // Use totalAmountDue for consistency
                        totalNetMilk = totalNetMilk,
                        grandSaleTotalForDate = grandTotalPrice,
                        avgRatePerLiter = avgRatePerLiter,
                        pdfSalesSummary = PdfSalesSummary(
                            totalQty = totalVolume.toRoundedStr(),
                            totalDeduction = totalDeduction.toRoundedStr(),
                            totalAmount = avgRatePerLiter.toPriceStr(),
                            totalPaid = avgRatePerLiter.toRoundedStr(),
                            balanceDue = avgRatePerLiter.toRoundedStr()
                        )
                    )
                }
            }
        }
    }

    /**
     * Helper function to check if all customers have a sales entry for the given date,
     * and inserts a blank entry if one is missing.
     */
    private suspend fun ensureSalesEntriesExist(date: LocalDate) {
        val allCustomers = repository.getAllCustomers().first()
        val existingSales = repository.getSalesByDateOnce(date)

        val missingCustomers = allCustomers.filterNot { customer ->
            existingSales.any { it.customer.customerId == customer.customerId }
        }

        missingCustomers.forEach { customer ->
            val newSale = SalesEntity(
                saleId = "${customer.customerId}_${date.toString()}",
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
    }


    // ----------------------------------------------------------
    // ⚡ Handle UI Events
    // ----------------------------------------------------------
    fun onEvent(event: SalesUiEvent) {
        when (event) {
            is SalesUiEvent.OnCustomerSelected ->
                _uiState.update { it.copy(navToLedgerForCustomerId = event.supplierId) }

            // SelectDate event now just updates the date and triggers the observation logic
            is SalesUiEvent.SelectDate ->
                onDateSelected(event.date)
        }
    }

    fun onLedgerNavigated() {
        _uiState.update { it.copy(navToLedgerForCustomerId = null) }
    }
}