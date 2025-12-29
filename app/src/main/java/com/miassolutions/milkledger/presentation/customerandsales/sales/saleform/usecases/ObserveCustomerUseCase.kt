package com.miassolutions.milkledger.presentation.customerandsales.sales.saleform.usecases

import com.miassolutions.milkledger.data.repository.CustomerRepository
import javax.inject.Inject

class ObserveCustomerUseCase @Inject constructor(
    private val repository: CustomerRepository
) {
    operator fun invoke() = repository.getAllCustomers()
}