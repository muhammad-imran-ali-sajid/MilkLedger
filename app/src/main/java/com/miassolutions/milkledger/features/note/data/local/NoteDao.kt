package com.miassolutions.milkledger.features.note.data.local


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    
    @Query("SELECT * FROM note_table")
    suspend fun getAllNotesForBackup(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("SELECT * FROM note_table WHERE noteId = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT * FROM note_table WHERE deletedAtMillis IS NULL ORDER BY createdAtMillis DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    // Soft Delete: Hum record delete nahi karte, sirf flag lagate hain
    @Query("UPDATE note_table SET deletedAtMillis = :deletedTime, isSynced = 0 WHERE noteId = :id")
    suspend fun softDeleteNote(id: String, deletedTime: Long = System.currentTimeMillis())
}