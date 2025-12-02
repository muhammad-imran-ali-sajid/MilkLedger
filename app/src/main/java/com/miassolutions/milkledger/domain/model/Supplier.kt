package com.miassolutions.milkledger.domain.model

data class Supplier(
    val id: String? = null, // null for new, not null for existing
    val name: String="",
    val rate: Double=0.0,
    val sortOrder: Int= 0,
    val advanceAmount : Double = 0.0,
    var isExpanded: Boolean = false
)



