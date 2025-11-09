package com.miassolutions.milkledger.data.repository

import android.util.Log
import com.miassolutions.milkledger.data.local.daos.NoteDao
import com.miassolutions.milkledger.data.local.entities.NoteEntity
import com.miassolutions.milkledger.data.mapper.toFirestoreModel
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
        private const val NOTES_COLLECTION = "notes"
        private const val TAG = "NoteRepository"
    }

    // --- Local Read Operations (Offline-First) ---

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getDoneNotes(): Flow<List<NoteEntity>> = noteDao.getDoneNotes()

    fun getPendingNotes(): Flow<List<NoteEntity>> = noteDao.getPendingNotes()

    suspend fun getNoteById(noteId: String): NoteEntity? = noteDao.getNoteById(noteId)

    // --- Local Write Operations + Remote Synchronization ---

    /**
     * Inserts or updates a note locally, then attempts to upload it to Firestore.
     */
    suspend fun insertOrUpdate(note: NoteEntity) {
        noteDao.insertOrUpdate(note)
        try {
            // Assuming NoteEntity has an 'id' field used as the documentId
            val firestoreModel = note.toFirestoreModel()
            firestoreSyncHelper.uploadSingle(
                collectionName = NOTES_COLLECTION,
                documentId = note.id,
                data = firestoreModel
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync insertOrUpdate for note ID: ${note.id}", e)
            // Local operation succeeded, but remote failed. A sync worker should handle retries.
        }
    }

    /**
     * Deletes a note locally, then attempts to delete it from Firestore.
     */
    suspend fun delete(note: NoteEntity) {
        val noteId = note.id
        noteDao.delete(note)

        try {
            // Delete the entity from the remote database
            firestoreSyncHelper.deleteDocument(
                collectionName = NOTES_COLLECTION,
                documentId = noteId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync delete for note ID: $noteId", e)
            // Local operation succeeded, but remote failed.
        }
    }

    /**
     * Updates the done state of a note locally, then attempts to update it on Firestore.
     * Note: This function requires fetching the entity after the DAO update to get the full object
     * for synchronization, unless the DAO supports updating the done state and returning the object.
     */
    suspend fun updateDoneState(noteId: String, isDone: Boolean) {
        noteDao.updateDoneState(noteId, isDone)

        // Fetch the updated entity for full synchronization payload
        val updatedNote = noteDao.getNoteById(noteId)

        if (updatedNote != null) {
            try {
                firestoreSyncHelper.uploadSingle(
                    collectionName = NOTES_COLLECTION,
                    documentId = updatedNote.id,
                    data = updatedNote.toFirestoreModel()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync done state update for note ID: $noteId", e)
            }
        }
    }

    // --- Bulk Operations ---

    // Keeping deleteAllNotes local-only for simplicity, as server bulk deletes are complex.
    suspend fun deleteAllNotes() = noteDao.deleteAllNotes()


    // --- Full Synchronization ---

    /**
     * Synchronizes all local notes with the remote Firestore database.
     * Requires the NoteDao to have:
     * 1. suspend fun getAllNotesList(): List<NoteEntity>
     * 2. suspend fun upsertAll(notes: List<NoteEntity>)
     */
    suspend fun synchronizeNotes() {
        Log.d(TAG, "Starting full note synchronization...")
        try {
            // 1. Download and merge remote changes
            val remoteNotes = firestoreSyncHelper.downloadCollection<NoteEntity>(NOTES_COLLECTION)
            if (remoteNotes.isNotEmpty()) {
                // Conflict resolution: remote updates overwrite local or new remote items are inserted.
                noteDao.upsertAll(remoteNotes)
                Log.d(TAG, "Downloaded and merged ${remoteNotes.size} items from Firestore.")
            } else {
                Log.d(TAG, "No remote items found to download.")
            }

            // 2. Upload all local changes (ensuring all local data is pushed)
            val allLocalNotes = noteDao.getAllNotesList()
            if (allLocalNotes.isNotEmpty()) {
                firestoreSyncHelper.uploadCollection(
                    collectionName = NOTES_COLLECTION,
                    dataList = allLocalNotes,
                    idExtractor = { it.id } // Explicitly use the 'id' field
                )
                Log.d(TAG, "Uploaded ${allLocalNotes.size} local items to Firestore.")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Full note synchronization failed: ${e.localizedMessage}", e)
            // The system remains operational due to local data, but sync failed.
        }
    }
}