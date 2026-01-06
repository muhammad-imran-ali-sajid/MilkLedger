package com.miassolutions.milkledger.features.milk

import com.miassolutions.milkledger.utils.extensions.toLocalDate

fun SaleDetailTuple.toDomain(): Sale {
    return Sale(
        saleId = this.milk.milkTransId,

        // Dates handling
        date = this.milk.dateMillis.toLocalDate(),
        paymentDate = this.paymentDate?.toLocalDate(), // Nullable handled automatically

        // Customer (Tuple me 'prefix' k zariye jo account aaya tha)
        customer = this.customer,

        // Milk Details
        volume = this.milk.quantity + (0.0), // Note: Agar aapne quantity me deduction minus kar k save ki thi to logic check kr len
        deduction = 0.0, // Filhal entity me deduction column nahi tha is liye 0
        rate = this.milk.rateUsed,

        // Payment
        amountPaid = this.paymentAmount ?: 0L, // Agar payment nahi hui to 0

        note = this.milk.notes
    )
}