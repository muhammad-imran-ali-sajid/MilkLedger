package com.miassolutions.milkledger.features.purchase.data

import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.ledger.LedgerEntryType
import com.miassolutions.milkledger.core.localdb.milk.MilkTransactionEntity
import com.miassolutions.milkledger.core.localdb.milk.TransactionType
import com.miassolutions.milkledger.utils.extensions.toDisplayDate
import com.miassolutions.milkledger.utils.extensions.toLongPaisa
import com.miassolutions.milkledger.utils.extensions.toMillis
import com.miassolutions.milkledger.utils.milkcalculations.MilkCalculationUtils
import java.time.LocalDate
import javax.inject.Inject

class PurchaseLedgerFactory @Inject constructor() {

    fun calculateAmounts(
        volume: Double,
        fat: Double,
        lr: Double,
        rate: Double
    ): PurchaseAmounts {
        val ts = MilkCalculationUtils.calculateTS(fat, lr, volume)
        val totalPricePaisa =
            MilkCalculationUtils.calculatePrice(volume, fat, lr, rate).toLongPaisa()

        return PurchaseAmounts(
            ts = ts,
            totalPricePaisa = totalPricePaisa
        )
    }

    fun createMilkTransaction(
        supplierId: String,
        date: LocalDate,
        paymentDate: LocalDate?,
        volume: Double,
        fat: Double,
        lr: Double,
        ts: Double,
        rate: Double,
        totalPricePaisa: Long,
        note: String?
    ): MilkTransactionEntity {
        return MilkTransactionEntity(
            accountId = supplierId,
            dateMillis = date.toMillis(),
            paymentDateMillis = paymentDate?.toMillis(),
            type = TransactionType.PURCHASE,
            volume = volume,
            fat = fat,
            lr = lr,
            ts = ts,
            quantity = volume,
            deduction = 0.0,
            rateUsed = rate,
            totalAmount = totalPricePaisa,
            notes = note
        )
    }

    fun createPurchaseLedger(
        date: LocalDate,
        supplierId: String,
        purchaseId: String,
        volume: Double,
        fat: Double,
        lr: Double,
        totalPricePaisa: Long
    ): FinancialLedgerEntity {
        return FinancialLedgerEntity(
            dateMillis = date.toMillis(),
            accountId = supplierId,
            referenceId = purchaseId,
            type = LedgerEntryType.MILK_PURCHASE,
            debit = 0,
            credit = totalPricePaisa,
            profitImpact = -totalPricePaisa,
            note = purchaseNote(volume, fat, lr)
        )
    }

    fun createCashPaidLedger(
        date: LocalDate,
        supplierId: String,
        purchaseId: String,
        amountPaid: Long,
        note: String
    ): FinancialLedgerEntity {
        return FinancialLedgerEntity(
            dateMillis = date.toMillis(),
            accountId = supplierId,
            type = LedgerEntryType.CASH_PAID,
            referenceId = purchaseId,
            debit = amountPaid,
            credit = 0,
            profitImpact = 0,
            note = note
        )
    }

    fun purchaseNote(volume: Double, fat: Double, lr: Double): String {
        return "Purchase: $volume Ltr (F:$fat, L:$lr)"
    }

    fun paymentNoteForSave(supplierName: String, date: LocalDate, paymentDate: LocalDate?): String {
        return if (paymentDate != null && !date.isEqual(paymentDate)) {
            "$supplierName\n(Dated: ${paymentDate.toDisplayDate()})"
        } else {
            supplierName
        }
    }

    fun paymentNoteForUpdate(supplierName: String, date: LocalDate, paymentDate: LocalDate?): String {
        return if (!date.isEqual(paymentDate)) {
            "$supplierName\n(Dated: ${paymentDate?.toDisplayDate()})"
        } else {
            supplierName
        }
    }
}

data class PurchaseAmounts(
    val ts: Double,
    val totalPricePaisa: Long
)
