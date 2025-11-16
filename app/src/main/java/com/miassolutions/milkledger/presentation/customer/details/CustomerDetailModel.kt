package com.miassolutions.milkledger.presentation.customer.details

import com.miassolutions.milkledger.core.pdf.customerreport.SalesItemRecord
import com.miassolutions.milkledger.core.pdf.purchasereport.PdfPurchaseItemRecord
import com.miassolutions.milkledger.core.pdf.salereport.PdfSalesItemRecord
import com.miassolutions.milkledger.core.util.toDisplayFormat
import com.miassolutions.milkledger.core.util.toPriceStr
import com.miassolutions.milkledger.core.util.toRoundedStr
import com.miassolutions.milkledger.data.local.relations.PurchaseWithSupplier
import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import java.time.LocalDate


data class CustomerDetailModel(
    val date: LocalDate,
    val milkAmount: Double,
    val deduction: Double,
    val netMilk: Double,
    val milkPrice: Double, // This is the total price/amount (Price * NetMilk)
    val payment: Double,
    val balance: Double,

    // 💡 Added fields for rate tracking (analogous to SupplierDetailModel)
    val rateUsed: Double,
    val newRate: Double,
    val isRateChanged: Boolean,
    val isRateChangeStart: Boolean = false, // Will be set by the extension function

    val notes: String? = null
)


// Assuming SaleWithCustomer has access to:
// - this.sale.rateUsed (price used for this specific sale)
// - this.customer.customerRate (current price for the customer)

fun SaleWithCustomer.toCustomerDetailModel(): CustomerDetailModel = CustomerDetailModel(
    date = this.sale.date,
    milkAmount = this.sale.volume,
    deduction = this.sale.deduction,
    netMilk = this.sale.netMilk,
    milkPrice = this.sale.price, // Assuming this is Total Price (NetMilk * rateUsed)
    payment = this.sale.paid,
    balance = this.sale.balance,

    // 💡 Added Rate Logic
    newRate = this.customer.customerRate,
    rateUsed = this.sale.rateUsed,
    isRateChanged = this.sale.rateUsed != this.customer.customerRate,

    notes = this.sale.notes
)


// Assumes you have a similar RecordItem structure for Customer
// Using Supplier's RecordItem for demonstration, but adjusting field names
fun List<CustomerDetailModel>.toRecordList(): List<SalesItemRecord> {

    return this.map { item ->
        SalesItemRecord(
            date = item.date.toDisplayFormat(),
            quantity = item.netMilk, // Use netMilk for quantity
            deduction = 0.0, // N/A for customer, or use a placeholder
            rate = item.rateUsed,
            amount = item.milkPrice, // Total amount/price
            paid = item.payment,
            balance = item.balance
        )
    }
}


fun List<SaleWithCustomer>.toSaleRecordList(): List<PdfSalesItemRecord> {
    return this.map { item ->
        PdfSalesItemRecord(
            customerName = item.customer.customerName,
            milkVolume = item.sale.volume.toPriceStr(),
            deduction = item.sale.deduction.toPriceStr(),
            rate = item.sale.rateUsed.toRoundedStr(),
            amount = item.sale.price.toRoundedStr(),
            paid = item.sale.paid.toPriceStr(),
            balance = item.sale.balance.toPriceStr()
        )

    }
}

// NEW EXTENSION FUNCTION to be used in the ViewModel after initial mapping

/**
 * Flags the first entry on which the rateUsed differs from the rateUsed on the previous day.
 */
fun List<CustomerDetailModel>.flagPriceChangeStarts(): List<CustomerDetailModel> {
    if (this.isEmpty()) return this

    val mutableList = this.toMutableList()

    // 1. Handle the first item: Flag it as a rate change start if its rateUsed
    // is different from the customer's current default (isRateChanged is true).
    if (mutableList[0].isRateChanged) {
        mutableList[0] = mutableList[0].copy(isRateChangeStart = true)
    }

    // 2. Iterate from the second item
    for (i in 1 until mutableList.size) {
        val current = mutableList[i]
        val previous = mutableList[i - 1]

        // Check if the current rateUsed is different from the previous rateUsed
        val hasRateChangedFromPreviousDay = current.rateUsed != previous.rateUsed

        if (hasRateChangedFromPreviousDay) {
            // This is the start of a new rate block. Flag it.
            mutableList[i] = current.copy(isRateChangeStart = true)
        }
    }

    return mutableList.toList()
}


