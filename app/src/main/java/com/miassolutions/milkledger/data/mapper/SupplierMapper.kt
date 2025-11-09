package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.SupplierEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreSupplier
import com.miassolutions.milkledger.domain.model.Supplier
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

fun Supplier.toEntity(): SupplierEntity = SupplierEntity(
    supplierId = this.id ?: UUID.randomUUID().toString(),
    supplierName = this.name,
    supplierRate = this.rate,
    sortOrder = this.sortOrder,
    advanceAmount = this.advanceAmount

)

fun SupplierEntity.toDomain(): Supplier = Supplier(
    id = this.supplierId,
    name = this.supplierName,
    rate = this.supplierRate,
    sortOrder = this.sortOrder,
    advanceAmount = this.advanceAmount,

    )


fun SupplierEntity.toFirestoreModel(): FirestoreSupplier {
    return FirestoreSupplier(
        supplierId = this.supplierId,
        supplierName = this.supplierName,
        supplierRate = this.supplierRate,
        sortOrder = this.sortOrder,
        advanceAmount = this.advanceAmount,
        // Conversion for Firestore ➡️
        createdAt = this.createdAt.toString(),
        isDefault = this.isDefault,
        isSynced = this.isSynced,
        updatedAt = this.updatedAt,
        deletedAt = this.deletedAt
    )
}


fun FirestoreSupplier.toRoomEntity(): SupplierEntity {
    return SupplierEntity(
        supplierId = this.supplierId,
        supplierName = this.supplierName,
        supplierRate = this.supplierRate,
        sortOrder = this.sortOrder,
        advanceAmount = this.advanceAmount,
        // Conversion for Room ⬅️
        createdAt = this.createdAt,
        isDefault = this.isDefault,
        isSynced = this.isSynced,
        updatedAt = this.updatedAt,
        deletedAt = this.deletedAt
    )
}

