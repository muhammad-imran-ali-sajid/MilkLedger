package com.miassolutions.milkledger.core.pdf

data class PdfReportModel(
    val fileName: String,       // e.g., "History_Ali_Store.pdf"
    val shopName: String,       // Header Top Left
    val reportTitle: String,    // e.g., "Supplier History"
    val dateRange: String,      // e.g., "1 Jan - 31 Jan"

    // Table Config
    val columnHeaders: List<String>, // ["Date", "Vol", "Rate", "Total"]
    val columnWeights: FloatArray,   // [2f, 1f, 1f, 2f] (Column ki choraai)

    // Data Rows (List of List of Strings)
    val rows: List<List<String>>,

    // Summary Bottom
    val summaryLabels: List<Pair<String, String>> // [("Total Milk", "500L"), ("Net Amount", "5000")]
)