package com.miassolutions.milkledger.domain.model

data class Customer(
    val id: Long? = null, // null for new, not null for existing
    val name: String,
    val phone: String,
    val email: String
)

