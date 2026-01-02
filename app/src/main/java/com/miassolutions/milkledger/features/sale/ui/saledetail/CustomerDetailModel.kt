package com.miassolutions.milkledger.features.sale.ui.saledetail


import com.miassolutions.milkledger.features.sale.ui.list.SaleWithCustomer
import com.miassolutions.milkledger.utils.extensions.toDisplayFormat
import com.miassolutions.milkledger.utils.extensions.toLocalDate
import com.miassolutions.milkledger.utils.pdf.customerreport.SalesItemRecord

import java.time.LocalDate




fun SaleWithCustomer.toCustomerDetailModel(): CustomerDetailModel = CustomerDetailModel(
    date = this.sale.dateMillis.toLocalDate(),
    milkAmount = this.sale.volume,
    deduction = this.sale.deduction,
    netMilk = this.sale.netMilk,
    milkPrice = this.sale.totalAmount, // Assuming this is Total Price (NetMilk * rateUsed)
    payment = this.sale.paid,
    balance = this.sale.balance,

    // 💡 Added Rate Logic
    rateUsed = this.sale.rateUsed,

    notes = this.sale.notes
)

data class CustomerDetailModel(
    val date: LocalDate,
    val rateUsed: Double,
    val milkAmount: Double,
    val netMilk: Double,
    val payment: Double,
    val balance: Double,
    val deduction: Double,
    val milkPrice: Double,

    val rateChanged: Boolean = false,
    val notes: String?
)



// Assumes you have a similar RecordItem structure for Customer
// Using Supplier's RecordItem for demonstration, but adjusting field names
fun List<CustomerDetailModel>.toRecordList(): List<SalesItemRecord> {

    return this.map { item ->
        SalesItemRecord(
            date = item.date.toDisplayFormat(),
            quantity = item.milkAmount, // Use netMilk for quantity
            deduction = 0.0, // N/A for customer, or use a placeholder
            rate = item.rateUsed,
            amount = item.milkPrice, // Total amount/price
            paid = item.payment,
            balance = item.balance,
            netMilk = item.netMilk
        )
    }
}


//fun List<SaleWithCustomerUI>.toSaleRecordList(): List<PdfSalesItemRecord> {
//    return this.map { item ->
//
//        PdfSalesItemRecord(
//            customerName = item.data.name,
//            milkVolume = item.data.volume,
//            deduction = item.data.deduction,
//            rate = item.data.rateUsed,
//            amount = item.data.price,
//            paid = item.data.paid,
//            balance = item.accumulatedBalance
//        )
//
//    }
//}





