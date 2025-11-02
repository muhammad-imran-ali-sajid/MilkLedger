package com.miassolutions.milkledger.core.di

import com.google.firebase.firestore.FirebaseFirestore
import com.miassolutions.milkledger.data.remote.FirestoreSyncHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirestoreSyncHelper(firestore: FirebaseFirestore): FirestoreSyncHelper {
        return FirestoreSyncHelper(firestore)
    }
}
