package com.miassolutions.milkledger.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity

data class PurchaseWithSupplier(
    @Embedded val purchase: PurchaseEntity,
    @Relation(
        parentColumn = "supplierId",
        entityColumn = "supplierId"
    )
    val supplier: SupplierEntity
)

