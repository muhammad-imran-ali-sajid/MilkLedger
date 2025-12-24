package com.miassolutions.milkledger.data.local.daos

import androidx.room.*
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    /* ---------------------------
       Sync / Raw
    --------------------------- */

    @Query("SELECT * FROM note_table")
    suspend fun getAllNotesList(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
        WHERE isDone = 1
        ORDER BY createdAtMillis DESC
    """)
    fun getDoneNotes(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM note_table
        WHERE isDone = 0
        ORDER BY createdAtMillis DESC
    """)
    fun getPendingNotes(): Flow<List<NoteEntity>>

    @Query("""
        SELECT * FROM note_table
        WHERE noteId = :id
        LIMIT 1
    """)
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("""
        UPDATE note_table
        SET isDone = :isDone
        WHERE noteId = :id
    """)
    suspend fun updateDoneState(id: String, isDone: Boolean)
}
