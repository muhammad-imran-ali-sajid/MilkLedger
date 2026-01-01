package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "profit_withdrawal_table",
    indices = [
        Index("dateMillis"),
        Index("receivedFromId")
    ]
)
data class ProfitWithdrawalEntity(
    @PrimaryKey val drawingId: String = UUID.randomUUID().toString(),

    val dateMillis: Long,          // Jis din paisa nikala
    val amount: Double,            // Kitna profit uthaya
    val note: String?,             // Note: "Ghar ke kharche ke liye", etc.
    val receivedFromId: String?,
    // System Fields
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
