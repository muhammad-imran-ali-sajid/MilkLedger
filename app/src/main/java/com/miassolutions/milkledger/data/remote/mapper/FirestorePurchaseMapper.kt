package com.miassolutions.milkledger.data.remote.mapper



import com.google.firebase.firestore.Exclude
import com.miassolutions.milkledger.data.local.entities.PurchaseEntity
import java.util.*

data class FirestorePurchaseModel(
    val purchaseId: String = UUID.randomUUID().toString(),
    val supplierId: String = "",
    val dateMillis: Long = 0L,
    val milkAmount: Double = 0.0,
    val fat: Double = 0.0,
    val lr: Double = 0.0,
    val ts: Double = 0.0,
    val milkPrice: Double = 0.0,
    val payment: Double = 0.0,
    val balance: Double = 0.0,
    val rateUsed: Double = 0.0,
    val notes: String? = null,

    // System fields
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
) {

    // Mapper: Convert Firestore model to PurchaseEntity (Room model)
    @Exclude
    fun toEntity(): PurchaseEntity {
        return PurchaseEntity(
            purchaseId = this.purchaseId,
            supplierId = this.supplierId,
            dateMillis = this.dateMillis,
            milkAmount = this.milkAmount,
            fat = this.fat,
            lr = this.lr,
            ts = this.ts,
            milkPrice = this.milkPrice,
            payment = this.payment,
            balance = this.balance,
            rateUsed = this.rateUsed,
            notes = this.notes,
            updatedAtMillis = this.updatedAtMillis,
            isSynced = this.isSynced,
            deletedAtMillis = this.deletedAtMillis
        )
    }

    companion object {
        // Mapper: Convert PurchaseEntity to FirestorePurchaseModel
        fun fromEntity(entity: PurchaseEntity): FirestorePurchaseModel {
            return FirestorePurchaseModel(
                purchaseId = entity.purchaseId,
                supplierId = entity.supplierId,
                dateMillis = entity.dateMillis,
                milkAmount = entity.milkAmount,
                fat = entity.fat,
                lr = entity.lr,
                ts = entity.ts,
                milkPrice = entity.milkPrice,
                payment = entity.payment,
                balance = entity.balance,
                rateUsed = entity.rateUsed,
                notes = entity.notes,
                updatedAtMillis = entity.updatedAtMillis,
                isSynced = entity.isSynced,
                deletedAtMillis = entity.deletedAtMillis
            )
        }
    }
}
