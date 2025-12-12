package com.miassolutions.milkledger.data.local.relations


import androidx.room.Embedded
import androidx.room.Relation
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import com.miassolutions.milkledger.data.local.entities.SupplierEntity

data class PurchaseWithAccumulation(
    @Embedded val purchase: PurchaseEntity,
    @Relation(
        parentColumn = "supplierId",
        entityColumn = "supplierId"
    )
    val supplier: SupplierEntity,
    val accumulatedBalance: Double = 0.0
)
