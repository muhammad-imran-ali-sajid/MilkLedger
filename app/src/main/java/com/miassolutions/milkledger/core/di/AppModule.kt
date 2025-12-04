package com.miassolutions.milkledger.core.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePrefs(app: Application): SharedPreferences {
        return app.getSharedPreferences("milkledger_prefs", Context.MODE_PRIVATE)
    }


}
