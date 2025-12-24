package com.miassolutions.milkledger.domain.model

import java.time.LocalDateTime

data class Note(
    val id: String,
    val title: String,
    val content: String,
    val isDone: Boolean,
    val createdAt: LocalDateTime,
    val alarmAt: LocalDateTime?
)
