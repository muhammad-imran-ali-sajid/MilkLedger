package com.miassolutions.milkledger.features.purchase.domain

import com.miassolutions.milkledger.features.account.domain.Account
import com.miassolutions.milkledger.features.purchase.data.MilkPurchaseRepository
import com.miassolutions.milkledger.features.purchase.model.UpdatePurchaseRequest
import java.time.LocalDate
import javax.inject.Inject

class SavePurchaseUseCase @Inject constructor(
    private val repository: MilkPurchaseRepository
) {
    suspend operator fun invoke(
        isEditMode: Boolean,
        purchaseId: String?,
        supplier: Account?,
        date: LocalDate,
        paymentDate: LocalDate,
        volumeStr: String,
        fatStr: String,
        lrStr: String,
        rateStr: String,
        paymentStr: String,
        note: String?
    ): Result<String> {

        // 1. Validations
        if (supplier == null) {
            return Result.failure(Exception("Please select a supplier"))
        }

        val volume = volumeStr.toDoubleOrNull() ?: 0.0
        val payment = paymentStr.toDoubleOrNull() ?: 0.0

        if (volume <= 0 && payment <= 0) {
            return Result.failure(Exception("Please enter volume or payment"))
        }

        // Fat/LR Validation
        val fat = fatStr.toDoubleOrNull() ?: 0.0
        val lr = lrStr.toDoubleOrNull() ?: 0.0

        val rate = rateStr.toDoubleOrNull() ?: 0.0
        val finalRate = if (rate.isNaN()) 0.0 else rate

        return try {
            if (isEditMode && purchaseId != null) {
                val updateRequest = UpdatePurchaseRequest(
                    purchaseId = purchaseId,
                    supplierId = supplier.accountId,
                    date = date,
                    paymentDate = paymentDate,
                    volume = volume,
                    fat = fat,
                    lr = lr,
                    rate = finalRate,
                    amountPaid = (payment * 100).toLong(),
                    note = note
                )
                repository.updateMilkPurchase(updateRequest)
                Result.success("Purchase Updated")
            } else {
                repository.saveMilkPurchase(
                    supplierId = supplier.accountId,
                    date = date,
                    paymentDate = paymentDate,
                    volume = volume,
                    fat = fat,
                    lr = lr,
                    rate = finalRate,
                    amountPaid = (payment * 100).toLong(),
                    note = note
                )
                Result.success("Purchase Saved")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}