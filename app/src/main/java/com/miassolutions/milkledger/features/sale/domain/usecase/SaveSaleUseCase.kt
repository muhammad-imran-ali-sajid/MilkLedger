package com.miassolutions.milkledger.features.sale.domain.usecase

import com.miassolutions.milkledger.features.sale.data.repository.SaleRepository
import com.miassolutions.milkledger.features.sale.domain.model.Sale
import javax.inject.Inject

class SaveSaleUseCase @Inject constructor(
    private val repository: SaleRepository
) {

    suspend operator fun invoke(sale: Sale) {
        repository.insertSale(sale)
    }
}