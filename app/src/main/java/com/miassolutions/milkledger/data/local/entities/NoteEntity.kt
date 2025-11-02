package com.miassolutions.milkledger.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val createdDate: LocalDate = LocalDate.now(),
    val alarmDate: LocalDate?,
    val isDone: Boolean = false
)
