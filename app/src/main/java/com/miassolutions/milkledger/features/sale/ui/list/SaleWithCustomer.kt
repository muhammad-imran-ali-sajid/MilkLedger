package com.miassolutions.milkledger.features.sale.ui.list

import androidx.room.Embedded
import androidx.room.Relation
import com.miassolutions.milkledger.features.customer.data.local.CustomerEntity
import com.miassolutions.milkledger.features.sale.data.local.SaleEntity

data class SaleWithCustomer(
    @Embedded val sale: SaleEntity,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "customerId"
    )
    val customer: CustomerEntity
)