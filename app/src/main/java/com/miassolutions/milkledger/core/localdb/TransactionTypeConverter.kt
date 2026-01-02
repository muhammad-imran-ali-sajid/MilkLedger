package com.miassolutions.milkledger.core.localdb

import androidx.room.TypeConverter
import com.miassolutions.milkledger.features.transaction.data.TransactionType

class TransactionTypeConverter {

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String {
        return type.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}
