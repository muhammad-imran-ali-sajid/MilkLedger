package com.miassolutions.milkledger.presentation.customer.customers.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomerUi(
    val id: String,
    val name: String,
    val displayRate: String,
    val sortOrder: Int,
    val displayAdvanceAmount: String,
    val isDefault: Boolean,
    val isExpanded: Boolean = false
) : Parcelable