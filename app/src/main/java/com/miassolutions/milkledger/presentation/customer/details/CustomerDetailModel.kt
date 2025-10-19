package com.miassolutions.milkledger.presentation.customer.details

import com.miassolutions.milkledger.data.local.relations.SaleWithCustomer
import java.time.LocalDate

data class CustomerDetailModel(
    val date : LocalDate,
    val milkAmount : Double,
    val deduction : Double,
    val netMilk : Double,
    val milkPrice : Double,
    val payment : Double,
    val balance : Double,
    val notes : String? = null
)


fun SaleWithCustomer.toCustomerDetail() : CustomerDetailModel = CustomerDetailModel(
    date = this.sale.date,
    milkAmount = this.sale.volume,
    deduction = this.sale.deduction,
    netMilk = this.sale.netMilk,
    milkPrice = this.sale.price,
    payment = this.sale.paid,
    balance = this.sale.balance,
    notes = this.sale.notes
)
