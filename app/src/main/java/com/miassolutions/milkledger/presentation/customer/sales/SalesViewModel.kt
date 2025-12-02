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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: SalesRepository,
    private val pRepo: PurchaseRepository
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
        loadPaidSales()

    }

    // State to hold the data, starting with an empty list
    private val _paidSalesList = MutableStateFlow<List<CustomerPaidSummary>>(emptyList())
    val paidSalesList: StateFlow<List<CustomerPaidSummary>> = _paidSalesList


    private fun loadPaidSales() {
        repository.getPaidSalesForDate(_uiState.value.currentDate)
            // Use onEach to update the StateFlow whenever the data changes in the DB
            .onEach { list ->
                _paidSalesList.value = list
                Log.d("SalesViewModel", "loadPaidSales: $list")
            }
            .catch { exception ->
                // Handle errors, e.g., log them or update a separate error StateFlow
                println("Error loading paid sales: $exception")
            }
            // Start collecting the Flow in the ViewModel's scope
            .launchIn(viewModelScope)
    }


    private val _balanceHistory = MutableStateFlow<List<BalanceHistory>>(emptyList())
    val balanceHistory: StateFlow<List<BalanceHistory>> get() = _balanceHistory


    fun getBalanceHistory(customerId: String) = viewModelScope.launch {
        // ⚠️ Redundancy Fix: Call the repository once
        val history = repository.getBalanceHistory(customerId)

        _balanceHistory.value = history

        // 💡 Logging the input ID is crucial for debugging
        Log.d(
            "SalesViewModel",
            "Loaded Balance History for ID: $customerId. Items: ${history.size}"
        )
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
            _uiState.update { it.copy(isLoading = true) } // START LOADING

            ensureSalesEntriesExist(date)


        // 2. Start the real-time observation job

            _uiState.update { it.copy(isLoading = true) } // START LOADING

            repository.getSalesByDate(date).collectLatest { sales ->
                val sortedSales = sales.sortedBy { it.customer.sortOrder }
                val saleList = sortedSales.map { it.toSalesList() }


                val totalVolume = saleList.sumOf { it.volume }
                val totalDeduction = saleList.sumOf { it.deduction }

                val validForAvg = saleList.filter { it.rate > 0.0 }
                val aTotalPrice = validForAvg.sumOf { it.price }

                val receivedAmount = validForAvg.sumOf { it.received }

                val aTotalVolume = validForAvg.sumOf { it.volume }

                // as per original ledger
                val purchaseTotalVolume =
                    pRepo.getPurchasesByDateOnce(date).sumOf { it.purchase.milkAmount }


                val grandTotalPrice = saleList.sumOf { it.price }
                val totalNetMilk = saleList.sumOf { it.netVolume }
                val totalBalance = saleList.sumOf { it.price - it.received }

                val avgRatePerLiter =
                    if (aTotalVolume > 0) aTotalPrice / purchaseTotalVolume else 0.0

                _uiState.update {
                    it.copy(
                        currentDate = date,
                        salesForDate = saleList,
                        totalMilk = totalVolume,
                        totalDeduction = totalDeduction,
                        totalBalance = totalBalance, // Use totalAmountDue for consistency
                        totalNetMilk = totalNetMilk,
                        grandSaleTotalForDate = grandTotalPrice,
                        receivedAmount = receivedAmount,
                        avgRatePerLiter = avgRatePerLiter,
                        isLoading = false,
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


}