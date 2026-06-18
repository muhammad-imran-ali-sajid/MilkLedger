package com.miassolutions.milkledger.features.sale.domain.usecase

import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import java.time.LocalDate
import javax.inject.Inject

class SaveSaleUseCase @Inject constructor(
    private val repository: MilkSaleRepository
) {
    suspend operator fun invoke(
        saleDate: LocalDate,
        paymentDate: LocalDate?,
        accountId: String,
        volume: Double,
        deduction: Double,
        rate: Double,
        amountPaid: Long,
        note: String?
    ) {
        repository.saveMilkSale(
            saleDate = saleDate,
            paymentDate = paymentDate,
            accountId = accountId,
            volume = volume,
            deduction = deduction,
            rate = rate,
            amountPaid = amountPaid,
            note = note
        )
    }
}
