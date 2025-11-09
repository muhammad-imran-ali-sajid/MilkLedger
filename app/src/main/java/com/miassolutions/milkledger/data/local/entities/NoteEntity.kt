package com.miassolutions.milkledger.data.local.entities


import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Parcelize
@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String= "",
    val content: String= "",
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val alarmDateTime: LocalDateTime?,
    val isDone: Boolean = false
) : Parcelable
