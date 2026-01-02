package com.miassolutions.milkledger.features.sale.ui.list

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.features.sale.data.repository.SaleRepository
import com.miassolutions.milkledger.features.sale.domain.model.Sale
import com.miassolutions.milkledger.features.sale.domain.model.toSaleUi
import com.miassolutions.milkledger.features.sale.domain.usecase.ObserveSalesForDateUseCase
import com.miassolutions.milkledger.features.sale.ui.list.SalesUiEffect.*
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SaleListViewModel @Inject constructor(
    private val observeSalesForDate: ObserveSalesForDateUseCase,
    private val salesRepository: SaleRepository
) : BaseViewModel<SalesUiState, SalesUiEvent, SalesUiEffect>(initialState = SalesUiState()) {

    private var observeJob: Job? = null

    init {
        observeDate(currentState.currentDate)
    }

    private fun loadBalanceHistory(
        customerId: String,
        customerName: String
    ) = viewModelScope.launch {

//        val history = salesRepository.getCustomerBalanceHistory(customerId)

//        Log.d("BalanceHistory", "History size = ${history.size}")

//        emitEffect(
//            SalesUiEffect.ShowBalanceHistory(
//                customerName = customerName,
//                historyItem = history
//            )
//        )
    }





    private fun observeDate(date: LocalDate) {
        observeJob?.cancel()

        observeJob = observeSalesForDate(date)
            .onStart {
                updateState {
                    it.copy(isLoading = true, currentDate = date)
                }
            }
            .onEach { summary ->
                val saleUiList = summary.sales.map { projections ->
                    projections.sale.toSaleUi(
                        customerName = projections.customerName,
                        accumulatedBalance = projections.accumulatedBalance
                    )
                }

                updateState {
                    it.copy(
                        isLoading = false,
                        sales = saleUiList,
                        totalMilk = summary.totalMilk,
                        totalDeduction = summary.totalDeduction,
                        totalNetMilk = summary.totalNetMilk,
                        totalAmount = summary.totalAmount,
                        receivedAmount = summary.receivedAmount,
                        totalBalance = summary.totalBalance,
                        avgRatePerLiter = summary.avgRate
                    )
                }
            }
            .launchIn(viewModelScope)

    }

    private fun deleteSale(saleId: String) = viewModelScope.launch {
        salesRepository.deleteSale(saleId)
        emitEffect(SalesUiEffect.ShowMessage("Sale deleted"))
    }

    private fun updateSale(event: SalesUiEvent.EditSale) = viewModelScope.launch {

        val existingSaleUi =
            currentState.sales.firstOrNull { it.id == event.saleId }
                ?: return@launch

        val updatedSale = Sale(
            id = existingSaleUi.id,
            customerId = existingSaleUi.customerId, // ✅ KEEP ORIGINAL
            date = existingSaleUi.date,              // ✅ KEEP ORIGINAL
            paidAt = event.paidAt,
            volume = event.volume,
            deduction = event.deduction,
            netMilk = event.volume - event.deduction,
            price = MilkCalculationUtils.calculateCustomerPrice(
                volume = event.volume,
                deduction = event.deduction,
                rate = event.rate
            ),
            paid = event.paid,
            balance = 0.0, // ledger-controlled
            rateUsed = event.rate,
            notes = event.notes
        )
        Log.d("SalesVM", "Updating sale: $updatedSale")

        salesRepository.updateSale(updatedSale)
    }



    override fun onEvent(event: SalesUiEvent) {
        when (event) {

            is SalesUiEvent.SelectDate ->
                observeDate(event.date)

            SalesUiEvent.NextDate ->
                observeDate(currentState.currentDate.plusDays(1))

            SalesUiEvent.PreviousDate ->
                observeDate(currentState.currentDate.minusDays(1))

            is SalesUiEvent.DeleteSale ->
                deleteSale(event.saleId)

            is SalesUiEvent.EditSale ->
                updateSale(event)

            is SalesUiEvent.EditClicked -> {
                emitEffect(
                    EditSaleRecord(event.saleId)
                )
            }


            is SalesUiEvent.OpenCustomerLedger ->
                emitEffect(
                    NavigateToCustomerLedger(
                        event.customerId,
                        event.customerName
                    )
                )

            is SalesUiEvent.BalanceClicked -> {
                loadBalanceHistory(event.customerId, event.customerName)
            }

            SalesUiEvent.DismissBalanceHistory -> {}
            SalesUiEvent.OpenSaleForm -> {
                emitEffect(NavigateToSaleForm)
            }
        }
    }


}
