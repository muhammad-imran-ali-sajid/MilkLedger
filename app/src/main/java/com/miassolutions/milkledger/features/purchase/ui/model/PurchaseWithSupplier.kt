package com.miassolutions.milkledger.features.purchase.ui.model

import androidx.room.Embedded
import androidx.room.Relation
import com.miassolutions.milkledger.features.purchase.data.PurchaseEntity
import com.miassolutions.milkledger.features.supplier.data.local.SupplierEntity

data class PurchaseWithSupplier(
    @Embedded val purchase: PurchaseEntity,
    @Relation(
        parentColumn = "supplierId",
        entityColumn = "supplierId"
    )
    val supplier: SupplierEntity
)