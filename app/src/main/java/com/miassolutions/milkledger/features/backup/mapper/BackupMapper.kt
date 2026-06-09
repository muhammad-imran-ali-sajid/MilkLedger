package com.miassolutions.milkledger.features.backup.mapper

import com.miassolutions.milkledger.core.localdb.account.local.AccountEntity
import com.miassolutions.milkledger.core.localdb.expense.ExpenseEntity
import com.miassolutions.milkledger.core.localdb.ledger.FinancialLedgerEntity
import com.miassolutions.milkledger.core.localdb.milk.MilkTransactionEntity
import com.miassolutions.milkledger.features.backup.model.dto.AccountBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.ExpenseBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.LedgerBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.MilkTransactionBackupDto
import com.miassolutions.milkledger.features.backup.model.dto.NoteBackupDto
import com.miassolutions.milkledger.features.note.data.local.NoteEntity

fun MilkTransactionEntity.toBackupDto(): MilkTransactionBackupDto {
    return MilkTransactionBackupDto(
        milkTransId = milkTransId,
        accountId = accountId,
        dateMillis = dateMillis,
        type = type.name,
        volume = volume,
        deduction = deduction,
        quantity = quantity,
        fat = fat,
        lr = lr,
        ts = ts,
        rateUsed = rateUsed,
        totalAmount = totalAmount,
        notes = notes,
        paymentDateMillis = paymentDateMillis,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        isSynced = isSynced,
        deletedAtMillis = deletedAtMillis
    )
}

fun AccountEntity.toBackupDto(): AccountBackupDto {
    return AccountBackupDto(
        accountId = accountId,
        name = name,
        phone = phone,
        accountType = accountType.name,
        isActive = isActive,
        sortOrder = sortOrder,
        advanceAmount = advanceAmount,
        defaultRate = defaultRate,
        initialBalance = initialBalance,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        isSynced = isSynced,
        deletedAtMillis = deletedAtMillis
    )
}

fun ExpenseEntity.toBackupDto(): ExpenseBackupDto {
    return ExpenseBackupDto(
        expenseId = expenseId,
        dateMillis = dateMillis,
        title = title,
        amount = amount,
        category = category,
        isPersonal = isPersonal,
        note = note,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        isSynced = isSynced,
        deletedAtMillis = deletedAtMillis
    )
}

fun FinancialLedgerEntity.toBackupDto(): LedgerBackupDto {
    return LedgerBackupDto(
        ledgerId = ledgerId,
        dateMillis = dateMillis,
        accountId = accountId,
        referenceId = referenceId,
        type = type.name,
        debit = debit,
        credit = credit,
        profitImpact = profitImpact,
        note = note,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        isSynced = isSynced,
        deletedAtMillis = deletedAtMillis
    )
}

fun NoteEntity.toBackupDto(): NoteBackupDto {
    return NoteBackupDto(
        noteId = noteId,
        title = title,
        content = content,
        alarmAtMillis = alarmAtMillis,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        isSynced = isSynced,
        deletedAtMillis = deletedAtMillis
    )
}