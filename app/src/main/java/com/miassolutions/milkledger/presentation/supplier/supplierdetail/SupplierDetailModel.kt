package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
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
    val notes: String? = null,

)

data class SupplierSummary(
    val summaryPeriod: String = "",
    val totalMilk: String = "",
    val totalTs: String = "",
    val totalPrice: String = "",
    val paidAmount: String = "",
    val balance: String = ""
)


fun PurchaseWithSupplier.toSupplierDetailModel(): SupplierDetailModel = SupplierDetailModel(
    date = this.purchase.date,
    milkAmount = this.purchase.milkAmount,
    fat = this.purchase.fat,
    lr = this.purchase.lr,
    ts = this.purchase.ts,
    milkPrice = this.purchase.milkPrice,
    payment = this.purchase.payment,
    balance = this.purchase.balance,
    rateUsed = this.purchase.rateUsed,
    notes = this.purchase.notes
)