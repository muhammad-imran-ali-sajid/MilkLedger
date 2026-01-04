package com.miassolutions.milkledger.core.localdb

import androidx.room.TypeConverter
import com.miassolutions.milkledger.core.localdb.account.local.AccountType
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.features.transaction.data.TransactionType

class TransactionTypeConverter {

//    @TypeConverter
//    fun fromTransactionType(type: TransactionType): String {
//        return type.name
//    }
//
//    @TypeConverter
//    fun toTransactionType(value: String): TransactionType {
//        return TransactionType.valueOf(value)
//    }

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

    // --- Transaction Type (Sale/Purchase) ---
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return try {
            TransactionType.valueOf(value)
        } catch (e: Exception) {
            TransactionType.SALE
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
