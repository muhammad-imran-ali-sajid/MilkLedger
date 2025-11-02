package com.miassolutions.milkledger.data.local.daos


import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // --- Insert or Update ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(note: NoteEntity)

    // --- Delete a single note ---
    @Delete
    suspend fun delete(note: NoteEntity)

    // --- Delete all notes (if needed) ---
    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()

    // --- Get all notes (ordered by creation date descending) ---
    @Query("SELECT * FROM note_table ORDER BY createdDate DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    // --- Get done notes ---
    @Query("SELECT * FROM note_table WHERE isDone = 1 ORDER BY createdDate DESC")
    fun getDoneNotes(): Flow<List<NoteEntity>>

    // --- Get pending notes ---
    @Query("SELECT * FROM note_table WHERE isDone = 0 ORDER BY createdDate DESC")
    fun getPendingNotes(): Flow<List<NoteEntity>>

    // --- Get a note by its ID ---
    @Query("SELECT * FROM note_table WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: String): NoteEntity?

    // --- Update the done state only ---
    @Query("UPDATE note_table SET isDone = :isDone WHERE id = :noteId")
    suspend fun updateDoneState(noteId: String, isDone: Boolean)
}
