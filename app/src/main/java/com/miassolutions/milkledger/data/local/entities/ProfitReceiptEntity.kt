package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "profit_receipt_table",
    indices = [
        Index("dateMillis"),
        Index("receivedFromId")
    ]
)
data class ProfitReceiptEntity(
    @PrimaryKey
    val receiptId: String = UUID.randomUUID().toString(),

    // Cash receive date
    val dateMillis: Long,

    /**
     * Employee / Owner / Partner ID
     * (future-proof)
     */
    val receivedFromId: String,

    // Cash actually received
    val amountReceived: Double,

    val note: String?,

    // System
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)
