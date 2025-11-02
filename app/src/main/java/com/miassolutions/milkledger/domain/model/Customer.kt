package com.miassolutions.milkledger.domain.model

data class Customer(
    val id: String? = null, // null for new, not null for existing
    val name: String,
    val rate: Double,
    val sortOrder : Int,
    val advanceAmount : Double
)

