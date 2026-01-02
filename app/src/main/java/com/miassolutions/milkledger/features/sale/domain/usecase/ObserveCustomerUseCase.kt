package com.miassolutions.milkledger.features.sale.domain.usecase

import com.miassolutions.milkledger.features.customer.data.repository.CustomerRepository
import javax.inject.Inject

class ObserveCustomerUseCase @Inject constructor(
    private val repository: CustomerRepository
) {
    operator fun invoke() = repository.getAllCustomers()
}