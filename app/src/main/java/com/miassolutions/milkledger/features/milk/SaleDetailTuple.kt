package com.miassolutions.milkledger.features.milk

import androidx.room.Embedded
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.milk.MilkTransactionEntity

data class SaleDetailTuple(
    // 1. Milk Entity (Pura object aa jayega)
    @Embedded val milk: MilkTransactionEntity,

    // 2. Account Entity (Prefix lagana zaroori hai taake columns mix na hon)
    @Embedded(prefix = "acc_") val customer: AccountEntity,

    // 3. Payment Fields (Ledger se aayenge)
    val paymentAmount: Long?, // Nullable (Shayad payment na di ho)
    val paymentDate: Long?    // Nullable
)
