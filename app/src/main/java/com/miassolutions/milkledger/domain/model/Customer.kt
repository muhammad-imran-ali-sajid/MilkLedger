package com.miassolutions.milkledger.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


//@Parcelize
//data class Customer(
//    val id: String? = null, // null for new, not null for existing
//    val name: String,
//    val rate: Double,
//    val sortOrder : Int,
//    val advanceAmount : Double,
//    var isExpanded : Boolean = false
//): Parcelable


data class Customer(
    val id: String,
    val name: String,
    val rate: Double,
    val sortOrder: Int,
    val advanceAmount: Double,
    val isDefault: Boolean
)

