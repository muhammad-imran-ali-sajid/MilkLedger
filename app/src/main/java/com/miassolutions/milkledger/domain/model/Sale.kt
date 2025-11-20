package com.miassolutions.milkledger.domain.model

data class Sale(
    val name: String,
    val volume: String,
    val deduction: String,
    val netVolume: String,
    val price: String,
    val received: String,
    val receivedDate: String,
    val balance: String
)
