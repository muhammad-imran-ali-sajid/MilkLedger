package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "profit_table")
data class ProfitEntity(
    @PrimaryKey
    val profitId: String = UUID.randomUUID().toString(),
    val receivedDate: LocalDate = LocalDate.now(),
    val netProfit : Double = 0.0,
    val grossProfit: Double = 0.0,
    val receivedProfit : Double = 0.0,
    val notes : String? = null,

    val isSynced: Boolean = false,
    val updatedAt: String = LocalDateTime.now().toString(),
    val deletedAt: LocalDateTime? = null
)
