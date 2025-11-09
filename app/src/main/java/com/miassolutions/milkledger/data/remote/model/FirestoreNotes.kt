package com.miassolutions.milkledger.data.remote.model

data class FirestoreNotes(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val createdDate: String = "",
    val alarmDate: String = "",
    val isDone: Boolean = false
)
