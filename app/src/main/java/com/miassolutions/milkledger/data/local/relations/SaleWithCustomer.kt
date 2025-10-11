package com.miassolutions.milkledger.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.local.entities.SalesEntryEntity

data class SaleWithCustomer(
    @Embedded val sale: SalesEntryEntity,
    @Relation(
        parentColumn = "customerId",
        entityColumn = "customerId"
    )
    val customer: CustomerEntity
)