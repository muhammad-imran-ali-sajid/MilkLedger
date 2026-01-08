package com.miassolutions.milkledger.core.localdb.ledger

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "financial_ledger_table",
    indices = [Index("accountId"), Index("dateMillis"), Index("referenceId")]
)
data class FinancialLedgerEntity(
    @PrimaryKey
    val ledgerId: String = UUID.randomUUID().toString(),

    val dateMillis: Long,
    val accountId: String,

    // Link to MilkTransaction or Expense
    val referenceId: String?,

    val type: LedgerEntryType,

    // --- Money Fields (Stored as Paisa) ---
    val debit: Long,                // Lena hai (Asset)
    val credit: Long,               // Dena hai (Liability)

    val profitImpact: Long,         // Sale (+), Expense (-), Return (-)

    val note: String?,

    // --- System Fields ---
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(), // ✅ Added
    val isSynced: Boolean = false,
    val deletedAtMillis: Long? = null // ✅ CRITICAL: Added for Soft Delete logic
)

enum class LedgerEntryType {
    MILK_SALE,
    CASH_RECEIVED,

    MILK_PURCHASE, // Doodh khareeda (Udhaar barha - Credit)
    CASH_PAID,      // Paisa diya (Udhaar kam hua - Debit)

    SALE_RETURN,      // ✅ Added: Taake report mein pata chale kitna maal wapis aaya
    PURCHASE_RETURN,  // ✅ Added

    EXPENSE,
    OWNER_WITHDRAWAL,
    OPENING_BALANCE
}