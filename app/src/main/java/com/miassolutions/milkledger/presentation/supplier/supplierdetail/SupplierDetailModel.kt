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

// NEW or REPLACED FUNCTION to be used in the ViewModel after initial mapping

/**
 * Flags the first entry on which the rateUsed differs from the rateUsed on the previous day.
 * This should replace or augment the flagConsecutiveRateChanges logic.
 */
fun List<SupplierDetailModel>.flagRateChangeStarts(): List<SupplierDetailModel> {
    if (this.isEmpty()) return this

    val mutableList = this.toMutableList()

    // The rate change alert should only show if the rate USED for this purchase
    // is different from the rate USED for the previous purchase.

    // 1. Check the very first item: if its rateUsed is different from the oldRate (supplier's default),
    // it's a rate change start (assuming the list starts after the change was made).
    // However, it's safer to compare with the previous item's rateUsed.

    // For the first item, we can't compare to a previous day. We'll only flag it if
    // it is flagged as 'isRateChanged' (i.e., different from the supplier's default).
    if (mutableList[0].isRateChanged) {
        mutableList[0] = mutableList[0].copy(isRateChangeStart = true)
    }

    for (i in 1 until mutableList.size) {
        val current = mutableList[i]
        val previous = mutableList[i - 1]

        // Check if the current rateUsed is different from the previous rateUsed
        // Note: Floating point comparison should ideally use an epsilon (tolerance),
        // but simple != is often used for simplicity with currency/rates.
        val hasRateChangedFromPreviousDay = current.rateUsed != previous.rateUsed

        if (hasRateChangedFromPreviousDay) {
            // This is the start of a new rate block. Flag it.
            mutableList[i] = current.copy(isRateChangeStart = true)
        }
    }

    return mutableList.toList()
}