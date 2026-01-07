package com.miassolutions.milkledger.core.localdb

import androidx.room.TypeConverter
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType

class TransactionTypeConverter {


    // --- Account Type ---
    @TypeConverter
    fun fromAccountType(type: AccountType): String {
        return type.name // "CUSTOMER" save hoga
    }

    @TypeConverter
    fun toAccountType(value: String): AccountType {
        return try {
            AccountType.valueOf(value)
        } catch (e: Exception) {
            AccountType.CUSTOMER // Fallback agar data corrupt ho
        }
    }


    // --- Ledger Entry Type ---
    @TypeConverter
    fun fromLedgerType(type: LedgerEntryType): String {
        return type.name
    }

    @TypeConverter
    fun toLedgerType(value: String): LedgerEntryType {
        return try {
            LedgerEntryType.valueOf(value)
        } catch (e: Exception) {
            LedgerEntryType.MILK_SALE
        }
    }
}
