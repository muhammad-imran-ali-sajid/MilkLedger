package com.miassolutions.milkledger.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.presentation.customer.sales.db.SalesEntity

data class SaleWithCustomer(
    @Embedded val sale: SalesEntity,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "customerId"
    )
    val customer: CustomerEntity
)