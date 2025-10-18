package com.miassolutions.sort_filter


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SortOption(
    val id: String,
    val title: String,
    var isSelected: Boolean = false,
    val ascending: Boolean = true
) : Parcelable
