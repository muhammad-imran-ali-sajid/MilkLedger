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
    val isConsecutiveRateChange: Boolean = false,
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

// NEW FUNCTION to be added in your data layer/helper utility where the list is prepared

/**
 * Iterates through a list of SupplierDetailModel (which must be sorted by date)
 * and flags items where the rate change is consecutive (i.e., the current item
 * AND the next item both have rate changes).
 */
fun List<SupplierDetailModel>.flagConsecutiveRateChanges(): List<SupplierDetailModel> {
    if (this.size < 2) return this

    // Use a mutable copy for modification during iteration
    val mutableList = this.toMutableList()

    for (i in 0 until mutableList.size - 1) {
        val current = mutableList[i]
        val next = mutableList[i + 1]

        // Check if both the current item and the next item had a rate change
        if (current.isRateChanged && next.isRateChanged) {
            // Flag both items as part of a consecutive change
            // Note: We use copy() to update the data class immutably
            mutableList[i] = current.copy(isConsecutiveRateChange = true)
            mutableList[i + 1] = next.copy(isConsecutiveRateChange = true)
        }
    }

    return mutableList.toList()
}