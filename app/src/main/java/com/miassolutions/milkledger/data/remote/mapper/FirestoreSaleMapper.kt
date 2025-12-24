package com.miassolutions.milkledger.data.remote.mapper

import com.google.firebase.firestore.Exclude
import com.miassolutions.milkledger.data.local.entities.SalesEntity
import java.util.*

data class FirestoreSaleModel(
    val saleId: String = UUID.randomUUID().toString(),    // Default to a new UUID, can be overridden when needed
    val customerId: String = "",                         // Default to empty string
    val dateMillis: Long = 0L,                           // Default to 0 (timestamp for sale)
    val paidAtMillis: Long? = null,                       // Nullable field, default to null
    val volume: Double = 0.0,                            // Default to 0.0
    val deduction: Double = 0.0,                         // Default to 0.0
    val netMilk: Double = 0.0,                           // Default to 0.0
    val price: Double = 0.0,                             // Default to 0.0
    val paid: Double = 0.0,                              // Default to 0.0
    val balance: Double = 0.0,                           // Default to 0.0
    val rateUsed: Double = 0.0,                          // Default to 0.0
    val notes: String? = null,                           // Nullable field for notes

    // System fields
    val updatedAtMillis: Long = System.currentTimeMillis(), // Default to current time
    val isSynced: Boolean = false,                       // Default to false (indicating unsynced state)
    val deletedAtMillis: Long? = null                    // Nullable field for deletion timestamp
) {

    // This function maps the FirestoreSaleModel to a SalesEntity
    @Exclude
    fun toEntity(): SalesEntity {
        return SalesEntity(
            saleId = this.saleId,
            customerId = this.customerId,
            dateMillis = this.dateMillis,
            paidAtMillis = this.paidAtMillis,
            volume = this.volume,
            deduction = this.deduction,
            netMilk = this.netMilk,
            price = this.price,
            paid = this.paid,
            balance = this.balance,
            rateUsed = this.rateUsed,
            notes = this.notes,
            updatedAtMillis = this.updatedAtMillis,
            isSynced = this.isSynced,
            deletedAtMillis = this.deletedAtMillis
        )
    }

    // Companion object for utility functions
    companion object {

        // This function maps a SalesEntity to a FirestoreSaleModel
        fun fromEntity(entity: SalesEntity): FirestoreSaleModel {
            return FirestoreSaleModel(
                saleId = entity.saleId,
                customerId = entity.customerId,
                dateMillis = entity.dateMillis,
                paidAtMillis = entity.paidAtMillis,
                volume = entity.volume,
                deduction = entity.deduction,
                netMilk = entity.netMilk,
                price = entity.price,
                paid = entity.paid,
                balance = entity.balance,
                rateUsed = entity.rateUsed,
                notes = entity.notes,
                updatedAtMillis = entity.updatedAtMillis,
                isSynced = entity.isSynced,
                deletedAtMillis = entity.deletedAtMillis
            )
        }

        // This function maps a list of SalesEntity objects to a list of FirestoreSaleModel objects
        fun fromEntityList(entities: List<SalesEntity>): List<FirestoreSaleModel> {
            return entities.map { fromEntity(it) }
        }
    }
}
