package com.miassolutions.milkledger.features.transaction.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

//@Entity(
//    tableName = "transaction_table",
//    indices = [
//        Index("dateMillis"),
//        Index("type"),
//        Index("referenceId"),
//        Index("accountId") // ✅ NEW
//    ]
//)
//data class TransactionEntity(
//    @PrimaryKey
//    val transactionId: String = UUID.randomUUID().toString(),
//
//    val dateMillis: Long,
//
//    val type: TransactionType,
//
//    /** saleId / purchaseId / expenseId */
//    val referenceId: String?,
//
//    /** ✅ customerId / supplierId / employeeId */
//    val accountId: String?,   // 🔥 THIS FIXES EVERYTHING
//
//    val debit: Double,
//    val credit: Double,
//    val profitImpact: Double,
//
//    val note: String?,
//
//    val createdAtMillis: Long = System.currentTimeMillis(),
//    val isSynced: Boolean = false,
//    val deletedAtMillis: Long? = null
//)

@Entity(
    tableName = "transaction_table",
    indices = [Index("dateMillis"), Index("accountId"), Index("referenceId"),Index("type")]
)
data class TransactionEntity(
    @PrimaryKey val transactionId: String = UUID.randomUUID().toString(),
    val dateMillis: Long,
    val type: TransactionType,

    val referenceId: String?,
    val accountId: String?,

    val debit: Double,
    val credit: Double,
    val profitImpact: Double,
    val note: String?,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)



enum class TransactionType {
    SALE,
    PURCHASE,
    EXPENSE,
    PROFIT_ADJUSTMENT,
    SALE_REVERSAL,
    PURCHASE_REVERSAL,
    EXPENSE_REVERSAL,
    OWNER_WITHDRAWAL // ✅ New: Jab maalik profit nikalega
}

