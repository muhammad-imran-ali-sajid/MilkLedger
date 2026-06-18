package com.miassolutions.milkledger.features.sale.data

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

class SaleLedgerFactory @Inject constructor() {

    fun calculateAmounts(volume: Double, deduction: Double, rate: Double): SaleAmounts {
        val netQuantity = volume - deduction
        val totalPricePaisa =
            MilkCalculationUtils.calculateCustomerPrice(volume, deduction, rate).toLongPaisa()

        return SaleAmounts(
            netQuantity = netQuantity,
            totalPricePaisa = totalPricePaisa
        )
    }

    fun createMilkTransaction(
        saleDate: LocalDate,
        paymentDate: LocalDate?,
        accountId: String,
        volume: Double,
        deduction: Double,
        rate: Double,
        totalPricePaisa: Long,
        netQuantity: Double,
        note: String?
    ): MilkTransactionEntity {
        return MilkTransactionEntity(
            accountId = accountId,
            dateMillis = saleDate.toMillis(),
            paymentDateMillis = paymentDate?.toMillis(),
            type = TransactionType.SALE,
            volume = volume,
            deduction = deduction,
            quantity = netQuantity,
            rateUsed = rate,
            totalAmount = totalPricePaisa,
            notes = note
        )
    }

    fun createSaleLedger(
        saleDate: LocalDate,
        accountId: String,
        saleId: String,
        volume: Double,
        deduction: Double,
        netQuantity: Double,
        totalPricePaisa: Long
    ): FinancialLedgerEntity {
        return FinancialLedgerEntity(
            dateMillis = saleDate.toMillis(),
            accountId = accountId,
            referenceId = saleId,
            type = LedgerEntryType.MILK_SALE,
            debit = totalPricePaisa,
            credit = 0,
            profitImpact = totalPricePaisa,
            note = saleNote(volume, deduction, netQuantity)
        )
    }

    fun createCashReceivedLedger(
        saleDate: LocalDate,
        accountId: String,
        saleId: String,
        amountPaid: Long,
        note: String
    ): FinancialLedgerEntity {
        return FinancialLedgerEntity(
            dateMillis = saleDate.toMillis(),
            accountId = accountId,
            type = LedgerEntryType.CASH_RECEIVED,
            referenceId = saleId,
            debit = 0,
            credit = amountPaid,
            profitImpact = 0,
            note = note
        )
    }

    fun saleNote(volume: Double, deduction: Double, netQuantity: Double): String {
        return "Sale: $volume - $deduction = $netQuantity L"
    }

    fun paymentNote(customerName: String, saleDate: LocalDate, paymentDate: LocalDate?): String {
        return if (paymentDate != null && !saleDate.isEqual(paymentDate)) {
            "$customerName\n(Dated: ${paymentDate.toDisplayDate()})"
        } else {
            customerName
        }
    }
}

data class SaleAmounts(
    val netQuantity: Double,
    val totalPricePaisa: Long
)
