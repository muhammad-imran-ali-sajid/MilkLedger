package com.miassolutions.milkledger.presentation.customerandsales.sales.saleslist

import androidx.lifecycle.viewModelScope
import com.miassolutions.milkledger.core.ui.BaseViewModel
import com.miassolutions.milkledger.core.util.MilkCalculationUtils
import com.miassolutions.milkledger.data.repository.SalesRepository
import com.miassolutions.milkledger.domain.model.Sale
import com.miassolutions.milkledger.domain.model.toSaleUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.acos

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val observeSalesForDate: ObserveSalesForDateUseCase,
    private val salesRepository: SalesRepository
) : BaseViewModel<SalesUiState, SalesUiEvent, SalesUiEffect>(initialState = SalesUiState()) {

    private var observeJob: Job? = null

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
        val updatedSale = Sale(
            id = event.saleId,
            customerId = "", // resolved internally or already known
            date = currentState.currentDate,
            paidAt = null,
            volume = event.volume,
            deduction = event.deduction,
            netMilk = event.volume - event.deduction,
            price = MilkCalculationUtils.calculateCustomerPrice(
                volume = event.volume,
                deduction = event.deduction,
                rate = event.rate
            ),
            paid = event.paid,
            balance = 0.0, // recalculated via ledger
            rateUsed = event.rate,
            notes = event.notes
        )

        salesRepository.updateSale(updatedSale)
        emitEffect(SalesUiEffect.ShowMessage("Sale updated"))
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

            is SalesUiEvent.OpenCustomerLedger ->
                emitEffect(
                    SalesUiEffect.NavigateToCustomerLedger(
                        event.customerId,
                        event.customerName
                    )
                )
        }
    }


}
