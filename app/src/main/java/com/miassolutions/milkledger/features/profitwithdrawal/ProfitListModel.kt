package com.miassolutions.milkledger.features.profitwithdrawal

import android.os.Parcelable

import com.miassolutions.milkledger.utils.extensions.toCompleteDateFormat
import com.miassolutions.milkledger.utils.pdf.profitreport.PdfProfitItemRecord
import kotlinx.parcelize.Parcelize
import java.time.LocalDate


@Parcelize
data class ProfitListModel(
    val id: String,
    val date: LocalDate,
    val netProfit: Double? = 0.0,
    val grossProfit: Double,
    val profitReceived: Double,
    val balance: Double? = 0.0,
    val notes: String?
) : Parcelable


fun List<ProfitListModel>.toProfitRecordList(): List<PdfProfitItemRecord> {
    return this.map { item ->
        PdfProfitItemRecord(
            date = item.date.toCompleteDateFormat(),
            grossProfit = item.grossProfit,
            netProfit = item.netProfit ?: 0.0,
            profitReceived = item.profitReceived,
            balance = item.balance ?: 0.0
        )
    }
}
