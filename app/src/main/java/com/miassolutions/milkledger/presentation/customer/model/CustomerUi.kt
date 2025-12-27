package com.miassolutions.milkledger.presentation.customer.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerUi(
    val id: String,
    val name: String,
    val rate: Double,
    val sortOrder: Int,
    val advanceAmount: Double,
    val isDefault: Boolean,
    val isExpanded: Boolean = false
) : Parcelable