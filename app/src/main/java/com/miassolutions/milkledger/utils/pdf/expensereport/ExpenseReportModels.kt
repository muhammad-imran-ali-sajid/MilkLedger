package com.miassolutions.milkledger.utils.pdf.expensereport

data class ExpenseReceiptPdf(
    val dateRange: String,
    val recordList: List<ExpenseItemRecord>,
    val totalExpenses: Long,
    val businessExpenses: Long,
    val personalExpenses : Long,
    val footerNote: String? = null
)

data class ExpenseItemRecord(
    val date: String,
    val expenseTitle: String,
    val expenseAmount: Long,
    val expenseType: String
)

data class PdfExpenseSummary(
    val totalExpenses : Long,
    val businessExpenses: Long,
    val personalExpenses : Long
)