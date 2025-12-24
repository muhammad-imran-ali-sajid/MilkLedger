package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "profit_table")
data class ProfitEntity(
    @PrimaryKey
    val profitId: String = UUID.randomUUID().toString(),

    val dateMillis: Long,

    val grossProfit: Double,
    val netProfit: Double,
    val receivedProfit: Double,
    val notes: String?,

    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)

