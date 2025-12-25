package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transaction_table",
    indices = [
        Index("dateMillis"),
        Index("type"),
        Index("referenceId")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val transactionId: String = UUID.randomUUID().toString(),

    // Ledger date
    val dateMillis: Long,

    /**
     * SALE
     * PURCHASE
     * EXPENSE
     * PROFIT_ADJUSTMENT
     */
    val type: TransactionType,

    /**
     * Related entity ID
     * saleId / purchaseId / expenseId / employeeId
     */
    val referenceId: String?,

    // Cash movement
    val debit: Double,   // money OUT
    val credit: Double,  // money IN

    // Profit effect of THIS transaction
    val profitImpact: Double,

    val note: String?,

    // System
    val createdAtMillis: Long = System.currentTimeMillis(),
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
    EXPENSE_REVERSAL
}

