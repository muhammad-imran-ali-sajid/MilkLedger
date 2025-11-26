package com.miassolutions.milkledger.presentation.profit

import android.os.Parcelable
import com.miassolutions.milkledger.core.pdf.profitreport.PdfProfitItemRecord
import com.miassolutions.milkledger.domain.model.Profit
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class ProfitListModel(
    val id : String,
    val date : LocalDate,
    val profit : Double,
    val profitReceived : Double,
    val balance : Double
): Parcelable





fun List<Profit>.toProfitRecordList(): List<PdfProfitItemRecord> {
    return this.map { item ->
        PdfProfitItemRecord(
            date = item.receivedDate.toString(),
            profit = item.netProfit,
            profitReceived = item.receivedProfit,
            balance = item.netProfit - item.receivedProfit
        )
    }
}
