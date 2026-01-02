package com.miassolutions.milkledger.features.sale.domain.usecase

import com.miassolutions.milkledger.features.sale.data.repository.SaleRepository
import java.time.LocalDate
import javax.inject.Inject

class CheckDuplicateSaleUseCase @Inject constructor(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(
        customerId: String,
        date: LocalDate
    ): Boolean {
        return repository.isDuplicateSale(customerId, date)
    }
}