package com.miassolutions.milkledger.data.local

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.miassolutions.milkledger.core.util.StaticDataHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class StaticDataCallback @Inject constructor(
    @ApplicationContext context: Context,
    private val provider: Provider<AppDatabase>
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        CoroutineScope(Dispatchers.IO).launch {
            val database = provider.get()
            val helper = StaticDataHelper(database)
            helper.insertStaticData()
        }
    }
}
