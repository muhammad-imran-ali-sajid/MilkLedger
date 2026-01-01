package com.miassolutions.milkledger.core.di

import android.content.Context
import com.miassolutions.milkledger.utils.premiumfeatures.FeatureManager
import com.miassolutions.milkledger.utils.premiumfeatures.PremiumFeaturePrefs
import com.miassolutions.milkledger.utils.premiumfeatures.RemoteConfigManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PremiumFeaturesModule {

    @Provides
    @Singleton
    fun providePrefs(@ApplicationContext context: Context) = PremiumFeaturePrefs(context)

    @Provides
    @Singleton
    fun provideRemoteConfig() = RemoteConfigManager()

    @Provides
    @Singleton
    fun provideFeatureManager(
        prefs: PremiumFeaturePrefs,
        remoteConfig: RemoteConfigManager
    ) = FeatureManager(prefs, remoteConfig)
}