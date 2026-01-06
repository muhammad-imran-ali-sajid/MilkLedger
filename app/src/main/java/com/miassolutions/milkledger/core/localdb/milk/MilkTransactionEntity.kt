package com.miassolutions.milkledger.core.localdb.milk

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "milk_transactions_table",
    indices = [Index("accountId"), Index("dateMillis")]
)
data class MilkTransactionEntity(
    @PrimaryKey
    val milkTransId: String = UUID.randomUUID().toString(),

    val accountId: String,          // Foreign Key (AccountEntity)
    val dateMillis: Long,
    val type: TransactionType,      // SALE, PURCHASE, RETURN

    // --- Measurements ---
    val quantity: Double,           // Liters
    val fat: Double? = null,        // Nullable for Sale
    val lr: Double? = null,
    val ts: Double? = null,

    // --- Financials ---
    val rateUsed: Double,

    // Money Field -> Stored as Paisa
    val totalAmount: Long,

    val notes: String?,

    // --- System Fields (For Sync & History) ---
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(), // ✅ Added for Edit Tracking
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)

enum class TransactionType {
    SALE, PURCHASE, SALE_RETURN, PURCHASE_RETURN
}