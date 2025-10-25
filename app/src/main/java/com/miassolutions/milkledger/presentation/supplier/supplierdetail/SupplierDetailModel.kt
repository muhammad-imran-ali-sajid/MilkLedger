package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import com.miassolutions.milkledger.core.pdf.RecordItem
import com.miassolutions.milkledger.core.ui.extensions.formattedDate
import com.miassolutions.milkledger.core.util.toRoundedStr
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
    val oldRate: Double,
    val isRateChanged: Boolean,
    val notes: String? = null,

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
    oldRate = this.supplier.supplierRate,
    rateUsed = this.purchase.rateUsed,
    isRateChanged = this.purchase.rateUsed != this.supplier.supplierRate,
    notes = this.purchase.notes
)

fun List<SupplierDetailModel>.toRecordList(): List<RecordItem> {
    return this.map { item ->
        RecordItem(
            date = item.date.formattedDate(),
            quantity = item.milkAmount,
            ts = item.ts,
            rate = item.rateUsed,
            amount = item.milkPrice,
            paid = item.payment,
            balance = item.balance
        )
    }
}