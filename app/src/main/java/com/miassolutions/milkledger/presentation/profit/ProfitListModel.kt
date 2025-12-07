package com.miassolutions.milkledger.presentation.profit

import android.os.Parcelable
import com.miassolutions.milkledger.core.pdf.profitreport.PdfProfitItemRecord
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.domain.model.Profit
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
            date = item.date.toDisplayFormat(),
            grossProfit = item.grossProfit,
            netProfit = item.netProfit ?: 0.0,
            profitReceived = item.profitReceived,
            balance = item.balance ?: 0.0
        )
    }
}
