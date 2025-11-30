package com.miassolutions.milkledger.core.pdf.expensereport

import com.miassolutions.milkledger.presentation.expenses.ExpenseType

data class ExpenseReceiptPdf(
    val dateRange: String,
    val recordList: List<ExpenseItemRecord>,
    val totalExpenses:String,
    val footerNote: String? = null
)

data class ExpenseItemRecord(
    val date: String,
    val expenseTitle: String,
    val expenseAmount: Double,
    val expenseType: String
)

data class PdfExpenseSummary(
    val totalExpenses : Double,
    val businessExpenses: Double,
    val personalExpenses : Double
)