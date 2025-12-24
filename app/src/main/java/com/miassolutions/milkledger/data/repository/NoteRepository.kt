package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.mapper.toFirestoreModel
import com.miassolutions.milkledger.data.mapper.toFirestoreModelList
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val firestoreSyncHelper: FirestoreSyncHelper
) {

    companion object {
        private const val COLLECTION = "notes"
        private const val TAG = "NoteRepository"
    }

    // -------------------------
    // LOCAL READ OPERATIONS
    // -------------------------
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    fun getPendingNotes(): Flow<List<NoteEntity>> = noteDao.getPendingNotes()
    fun getDoneNotes(): Flow<List<NoteEntity>> = noteDao.getDoneNotes()
    suspend fun getNoteById(noteId: String): NoteEntity? = noteDao.getNoteById(noteId)
    suspend fun getAllNotesList(): List<NoteEntity> = noteDao.getAllNotesList()

    // -------------------------
    // LOCAL WRITE + FIRESTORE SYNC
    // -------------------------

    /** Inserts or updates a note locally, then syncs to Firestore */
    suspend fun upsert(note: NoteEntity) {
        noteDao.insertOrUpdate(note)
        try {
            firestoreSyncHelper.uploadSingle(
                collectionName = COLLECTION,
                documentId = note.id,
                data = note.toFirestoreModel()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync note ${note.id}", e)
        }
    }

    /** Updates the done state of a note */
    suspend fun updateDoneState(noteId: String, isDone: Boolean) {
        noteDao.updateDoneState(noteId, isDone)
        val updatedNote = noteDao.getNoteById(noteId)
        if (updatedNote != null) {
            try {
                firestoreSyncHelper.uploadSingle(
                    collectionName = COLLECTION,
                    documentId = updatedNote.id,
                    data = updatedNote.toFirestoreModel()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync done state for note ${noteId}", e)
            }
        }
    }

    /** Deletes a note locally and from Firestore */
    suspend fun delete(note: NoteEntity) {
        noteDao.delete(note)
        try {
            firestoreSyncHelper.deleteDocument(
                collectionName = COLLECTION,
                documentId = note.id
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete for note ${note.id}", e)
        }
    }

    /** Deletes all notes locally only */
    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }

    // -------------------------
    // FULL SYNC / RESTORE
    // -------------------------

    /** Downloads all notes from Firestore and merges locally */
    suspend fun restoreAllNotes() {
        Log.d(TAG, "Restoring notes from Firestore...")
        try {
            val remoteNotes = firestoreSyncHelper.downloadCollection<NoteEntity>(COLLECTION)
            if (remoteNotes.isNotEmpty()) {
                noteDao.upsertAll(remoteNotes)
                Log.d(TAG, "Restored ${remoteNotes.size} notes from Firestore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore notes", e)
        }
    }

    /** Two-way sync: download remote and upload local */
    suspend fun synchronizeNotes() {
        Log.d(TAG, "Starting full note synchronization...")
        try {
            // 1️⃣ Download remote notes and merge
            val remoteNotes = firestoreSyncHelper.downloadCollection<NoteEntity>(COLLECTION)
            if (remoteNotes.isNotEmpty()) {
                noteDao.upsertAll(remoteNotes)
                Log.d(TAG, "Downloaded and merged ${remoteNotes.size} notes")
            }

            // 2️⃣ Upload local notes
            val localNotes = noteDao.getAllNotesList()
            if (localNotes.isNotEmpty()) {
//                firestoreSyncHelper.uploadCollection(
//                    collectionName = COLLECTION,
//                    dataList = localNotes.toFirestoreModelList(),
//                    idExtractor = { it.id }
//                )
//                Log.d(TAG, "Uploaded ${localNotes.size} local notes to Firestore")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Full synchronization failed", e)
        }
    }
}
