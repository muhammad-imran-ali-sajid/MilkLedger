package com.miassolutions.milkledger.features.purchase.ui.supplierhistory

import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.pdf.supplierreport.PdfSupplierItemRecord
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


fun List<SupplierDetailModel>.toRecordList(): List<PdfSupplierItemRecord> {

    return this.map { item ->
        PdfSupplierItemRecord(
            date = item.date.toCompleteDateFormat(),
            quantity = item.milkAmount,
            ts = item.ts,
            rate = item.rateUsed,
            amount = item.milkPrice,
            paid = item.payment,
            balance = item.balance
        )
    }
}


