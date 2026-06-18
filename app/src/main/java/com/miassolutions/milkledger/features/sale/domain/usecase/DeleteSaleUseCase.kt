package com.miassolutions.milkledger.features.sale.domain.usecase

import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import javax.inject.Inject

class DeleteSaleUseCase @Inject constructor(
    private val repository: MilkSaleRepository
) {
    suspend operator fun invoke(saleId: String) {
        repository.deleteSale(saleId)
    }
}
