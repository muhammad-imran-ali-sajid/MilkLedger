package com.miassolutions.milkledger.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

//@Entity(tableName = "expense_table")
//data class ExpensesEntity(
//    @PrimaryKey
//    val expenseId: String = UUID.randomUUID().toString(),
//
//    val dateMillis: Long,
//    val expenseTitle: String,
//    val expenseAmount: Double,
//    val expenseNote: String?,
//    val isBusiness: Boolean,
//
//    val createdAtMillis: Long = System.currentTimeMillis(),
//    val updatedAtMillis: Long = System.currentTimeMillis(),
//    val isSynced: Boolean = false,
//    val deletedAtMillis: Long? = null
//)

@Entity(tableName = "expense_table", indices = [Index("dateMillis")])
data class ExpensesEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),
    val dateMillis: Long,
    val expenseTitle: String,
    val expenseAmount: Double,
    val note: String?,

    val isBusiness: Boolean,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)