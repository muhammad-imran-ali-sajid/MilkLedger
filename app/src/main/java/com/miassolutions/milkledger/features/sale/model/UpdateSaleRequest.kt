package com.miassolutions.milkledger.features.sale.model

import java.time.LocalDate


data class UpdateSaleRequest(
    val saleId: String,          // Kisko update krna hai
    val accountId: String,       // ID confirm krny k liye
    val date: LocalDate,         // Nayi Date
    val paymentDate: LocalDate?,
    val volume: Double,          // Naya Doodh
    val deduction: Double,       // Nayi Katoti
    val rate: Double,            // Naya Rate
    val amountPaid: Long,        // Nayi Payment (Agar change hui)
    val note: String?            // Naya Note
)