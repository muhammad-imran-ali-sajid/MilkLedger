package com.miassolutions.milkledger.features.sale.domain.usecase

import com.miassolutions.milkledger.features.sale.data.MilkSaleRepository
import com.miassolutions.milkledger.features.sale.model.UpdateSaleRequest
import javax.inject.Inject

class UpdateSaleUseCase @Inject constructor(
    private val repository: MilkSaleRepository
) {
    suspend operator fun invoke(request: UpdateSaleRequest) {
        repository.updateMilkSale(request)
    }
}
