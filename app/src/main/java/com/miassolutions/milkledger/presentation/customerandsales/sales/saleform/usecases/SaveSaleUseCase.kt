package com.miassolutions.milkledger.presentation.customerandsales.sales.saleform.usecases

import com.miassolutions.milkledger.data.repository.SalesRepository
import com.miassolutions.milkledger.domain.model.Sale
import javax.inject.Inject

class SaveSaleUseCase @Inject constructor(
    private val repository: SalesRepository
) {

    suspend operator fun invoke(sale: Sale) {
        repository.insertSale(sale)
    }
}