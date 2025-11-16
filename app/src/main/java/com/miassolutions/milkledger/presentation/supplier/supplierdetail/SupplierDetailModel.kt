package com.miassolutions.milkledger.presentation.supplier.supplierdetail

import com.miassolutions.milkledger.core.pdf.purchasereport.PdfPurchaseItemRecord
import com.miassolutions.milkledger.core.pdf.supplierreport.PdfSupplierItemRecord
import com.miassolutions.milkledger.core.util.toDisplayFormat
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
    val newRate: Double,
    val isRateChanged: Boolean,
    val isRateChangeStart: Boolean = false,
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
    newRate = this.supplier.supplierRate,
    rateUsed = this.purchase.rateUsed,
    isRateChanged = this.purchase.rateUsed != this.supplier.supplierRate,
    notes = this.purchase.notes
)

fun List<SupplierDetailModel>.toRecordList(): List<PdfSupplierItemRecord> {

    return this.map { item ->
        PdfSupplierItemRecord(
            date = item.date.toDisplayFormat(),
            quantity = item.milkAmount,
            ts = item.ts,
            rate = item.rateUsed,
            amount = item.milkPrice,
            paid = item.payment,
            balance = item.balance
        )
    }
}

fun List<PurchaseWithSupplier>.toPurchaseRecordList(): List<PdfPurchaseItemRecord> {
    return this.map { item ->
        PdfPurchaseItemRecord(
            supplierName = item.supplier.supplierName,
            milkVolume = item.purchase.milkAmount.toRoundedStr(),
            fat = item.purchase.fat.toRoundedStr(),
            lr = item.purchase.lr.toRoundedStr(),
            ts = item.purchase.ts.toRoundedStr(),
            rate = item.supplier.supplierRate.toRoundedStr(),
            amount = item.purchase.milkPrice.toRoundedStr(),
            paid = item.purchase.payment.toRoundedStr(),
            balance = item.purchase.balance.toRoundedStr()
        )

    }
}

// NEW or REPLACED FUNCTION to be used in the ViewModel after initial mapping

/**
 * Flags the first entry on which the rateUsed differs from the rateUsed on the previous day.
 * This should replace or augment the flagConsecutiveRateChanges logic.
 */
fun List<SupplierDetailModel>.flagRateChangeStartsUniversal(): List<SupplierDetailModel> {
    if (this.size <= 1) return this

    // Detect sorting order using the first and last comparable item (date)
    val firstDate = this.first().date
    val lastDate = this.last().date
    val isDescending = firstDate.isAfter(lastDate)

    val mutableList = this.toMutableList()

    // Depending on order, adjust the iteration logic
    if (!isDescending) {
        // ASCENDING (oldest → newest)
        // Compare current with previous
        if (mutableList.first().isRateChanged) {
            mutableList[0] = mutableList.first().copy(isRateChangeStart = true)
        }

        for (i in 1 until mutableList.size) {
            val prev = mutableList[i - 1]
            val curr = mutableList[i]
            val changed = curr.rateUsed != prev.rateUsed
            mutableList[i] = curr.copy(isRateChangeStart = changed)
        }
    } else {
        // DESCENDING (newest → oldest)
        // Compare current with next
        if (mutableList.first().isRateChanged) {
            mutableList[0] = mutableList.first().copy(isRateChangeStart = true)
        }

        for (i in 0 until mutableList.size - 1) {
            val curr = mutableList[i]
            val next = mutableList[i + 1]
            val changed = curr.rateUsed != next.rateUsed
            mutableList[i] = curr.copy(isRateChangeStart = changed)
        }
    }

    return mutableList
}


