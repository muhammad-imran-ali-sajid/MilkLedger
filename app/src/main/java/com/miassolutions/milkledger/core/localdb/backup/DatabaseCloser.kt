package com.miassolutions.milkledger.core.localdb.backup

import com.miassolutions.milkledger.core.localdb.database.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseCloser @Inject constructor(
    private val db: AppDatabase
) {
    fun close() {
        if (db.isOpen) {
            db.close()
        }
    }
}
