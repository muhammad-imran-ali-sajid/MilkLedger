package com.miassolutions.milkledger.core.pdf

data class PdfReportModel(
    val fileName: String,
    val shopName: String,
    val reportTitle: String,
    val dateRange: String,

    val columnHeaders: List<String>,
    val columnWeights: FloatArray,

    val rows: List<List<String>>,

    //Table ke sab se neeche wali line (Footer)
    val summaryRow: List<String>? = null,

    // Bottom Summary (Optional ab, kyunke table me summary agayi hai)
    val summaryLabels: List<Pair<String, String>> = emptyList()
)