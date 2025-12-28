package com.miassolutions.milkledger.presentation.customerandsales.sales.addsale.usecases

import com.miassolutions.milkledger.data.repository.SalesRepository
import java.time.LocalDate
import javax.inject.Inject

class CheckDuplicateSaleUseCase @Inject constructor(
    private val repository: SalesRepository
) {
    suspend operator fun invoke(
        customerId: String,
        date: LocalDate
    ): Boolean {
        return repository.isDuplicateSale(customerId, date)
    }
}