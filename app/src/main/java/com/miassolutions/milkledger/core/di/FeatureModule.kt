package com.miassolutions.milkledger.core.di

import android.content.Context
import com.miassolutions.milkledger.core.feature.FeatureManager
import com.miassolutions.milkledger.core.feature.FeaturePrefs
import com.miassolutions.milkledger.core.feature.RemoteConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeatureModule {

    @Provides
    @Singleton
    fun providePrefs(@ApplicationContext context: Context) = FeaturePrefs(context)

    @Provides
    @Singleton
    fun provideRemoteConfig() = RemoteConfigManager()

    @Provides
    @Singleton
    fun provideFeatureManager(
        prefs: FeaturePrefs,
        remoteConfig: RemoteConfigManager
    ) = FeatureManager(prefs, remoteConfig)
}