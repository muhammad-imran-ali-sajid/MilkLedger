package com.miassolutions.milkledger.core.localdb.account.local


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
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
    val isActive: Boolean = true, // Default True

    val sortOrder: Int = 0,
    val advanceAmount: Long?,


    val defaultRate: Double = 0.0,

    // Money Field -> Stored as Paisa (e.g., 500000 = Rs 5000.00)
    val initialBalance: Long?,

    val createdAtMillis: Long,
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null
): Parcelable

enum class AccountType { CUSTOMER, SUPPLIER, OWNER }