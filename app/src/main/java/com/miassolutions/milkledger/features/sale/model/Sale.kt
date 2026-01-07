package com.miassolutions.milkledger.features.sale.model


// Note: Agar aapke paas alag 'Account' domain model hai to wo import karein,
// filhal simplicity k liye Entity use kar raha hoon (ya aap Account domain model use karein).
import android.os.Parcelable
import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Sale(
    val saleId: String,

    // Dates
    val date: LocalDate,          // Doodh ki tareekh (Inventory)
    val paymentDate: LocalDate?,  // Paise dene ki tareekh (Cash Ledger)

    // Customer Info (Pura object taake Edit form me naam/rate show ho sakay)
    val customer: AccountEntity,

    // Milk Stats
    val volume: Double,
    val deduction: Double,
    val rate: Double,

    // Financials (Paisa)
    val amountPaid: Long,

    // Extra
    val note: String? = null
) : Parcelable {

    // --- Helper Properties (UI me direct use karne k liye) ---

    val netQuantity: Double
        get() = volume - deduction

    // Total Bill (Calculated automatically)
    // Formula: (Net * Rate) -> Paisa conversion
    val totalAmount: Long
        get() = ((netQuantity * rate) * 100).toLong()

    val remainingBalance: Long
        get() = totalAmount - amountPaid
}