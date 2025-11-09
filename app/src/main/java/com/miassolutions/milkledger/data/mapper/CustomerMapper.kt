package com.miassolutions.milkledger.data.mapper

import com.miassolutions.milkledger.data.local.entities.CustomerEntity
import com.miassolutions.milkledger.data.remote.model.FirestoreCustomer
import com.miassolutions.milkledger.domain.model.Customer
import java.util.UUID

fun Customer.toEntity(): CustomerEntity = CustomerEntity(
    customerId = this.id ?: UUID.randomUUID().toString(),
    customerName = this.name,
    customerRate = this.rate,
    sortOrder = this.sortOrder,
    advanceAmount = this.advanceAmount

)

fun CustomerEntity.toDomain(): Customer = Customer(
    id = this.customerId,
    name = this.customerName,
    rate = this.customerRate,
    sortOrder = this.sortOrder,
    advanceAmount = this.advanceAmount
)


fun CustomerEntity.toFirestoreModel(): FirestoreCustomer {
    return FirestoreCustomer(
        customerId = this.customerId,
        customerName = this.customerName,
        customerRate = this.customerRate,
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


fun FirestoreCustomer.toRoomEntity(): CustomerEntity {
    return CustomerEntity(
        customerId = this.customerId,
        customerName = this.customerName,
        customerRate = this.customerRate,
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


