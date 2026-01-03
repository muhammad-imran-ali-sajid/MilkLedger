package com.miassolutions.milkledger.core.localdb.expense

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "expense_table", indices = [Index("dateMillis")])
data class ExpenseEntity(
    @PrimaryKey
    val expenseId: String = UUID.randomUUID().toString(),

    val dateMillis: Long,
    val title: String,

    // Money Field -> Stored as Paisa
    val amount: Long,

    val category: String? = null,           // Petrol, Rent, Food, etc.
    val isPersonal: Boolean,        // true = Withdrawal (Malik ka), false = Business Expense
    val note: String?,

    // --- System Fields ---
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(), // ✅ Added for Edit tracking
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)