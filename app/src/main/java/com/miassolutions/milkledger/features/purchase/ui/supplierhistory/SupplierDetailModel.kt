package com.miassolutions.milkledger.features.purchase.ui.supplierhistory


import java.time.LocalDate

data class SupplierDetailModel(

    val date: LocalDate,
    val milkAmount: Double,
    val fat: Double,
    val lr: Double,
    val ts: Double,
    val milkPrice: Double,
    val payment: Double,
    val balance: Double,
    val rateUsed: Double,
    val isRateChanged: Boolean = false,
    val notes: String? = null,

    )





