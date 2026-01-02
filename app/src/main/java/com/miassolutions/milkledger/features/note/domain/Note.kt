package com.miassolutions.milkledger.features.note.domain

import java.time.LocalDateTime

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime,
    val alarmAt: LocalDateTime?
)