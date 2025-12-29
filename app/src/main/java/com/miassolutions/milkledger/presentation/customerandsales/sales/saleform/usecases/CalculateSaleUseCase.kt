package com.miassolutions.milkledger.presentation.customerandsales.sales.saleform.usecases

import javax.inject.Inject

class CalculateSaleUseCase @Inject constructor(){

    operator fun invoke(
        volume: String,
        deduction: String,
        rate: Double,
        received: String
    ): CalculateResult {
        val v = volume.toDoubleOrNull() ?: 0.0
        val d = deduction.toDoubleOrNull() ?: 0.0
        val r = received.toDoubleOrNull() ?: 0.0

        val net = (v - d).coerceAtLeast(0.0)
        val price = net * rate
        val balance = price - r

        return CalculateResult(
            netMilk = net,
            price = price,
            balance = balance
        )
    }
}


data class CalculateResult(
    val netMilk: Double,
    val price: Double,
    val balance: Double
)