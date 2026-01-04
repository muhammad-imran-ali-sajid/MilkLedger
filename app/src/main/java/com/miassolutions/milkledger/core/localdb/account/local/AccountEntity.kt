package com.miassolutions.milkledger.core.localdb.account.local


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "accounts_table",
    indices = [Index(value = ["sortOrder", "accountType"])]
)
data class AccountEntity(
    @PrimaryKey
    val accountId: String,
    val name: String,
    val phone: String?,
    val accountType: AccountType, // Enum niche defined hai

    val sortOrder: Int = 0,
    val advanceAmount: Long?,

    // Rate Currency me hota hai lekin usually points me ho sakta hai (e.g 220.5)
    // Is liye Rate ko Double rakhna behtar hai, lekin Balance ko Long.
    val defaultRate: Double = 0.0,

    // Money Field -> Stored as Paisa (e.g., 500000 = Rs 5000.00)
    val initialBalance: Long?,

    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
)

enum class AccountType { CUSTOMER, SUPPLIER, OWNER }