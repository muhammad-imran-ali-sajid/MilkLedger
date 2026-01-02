package com.miassolutions.milkledger.features.note.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /* ---------------------------
       Sync / Raw
    --------------------------- */

    @Query("SELECT * FROM note_table")
    suspend fun getAllNotesList(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM note_table")
    suspend fun clearAll()

    @Query("DELETE FROM note_table WHERE noteId = :id")
    suspend fun deleteById(id: String)

    /* ---------------------------
       Queries
    --------------------------- */

    @Query("""
        SELECT * FROM note_table
        ORDER BY createdAtMillis DESC
    """)
    fun getAllNotes(): Flow<List<NoteEntity>>


    @Query("""
        SELECT * FROM note_table
        WHERE noteId = :id
        LIMIT 1
    """)
    suspend fun getNoteById(id: String): NoteEntity?


}