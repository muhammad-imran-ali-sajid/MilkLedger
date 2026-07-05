package com.miassolutions.milkledger.features.remoteconfig.di

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.miassolutions.milkledger.features.remoteconfig.data.RemoteConfigDataSource
import com.miassolutions.milkledger.features.remoteconfig.domain.FeatureFlagsRepository
import com.miassolutions.milkledger.features.remoteconfig.domain.FeatureFlagsRepositoryImpl
import com.miassolutions.milkledger.features.remoteconfig.model.FeatureFlags
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteConfigModule {

    @Binds
    @Singleton
    abstract fun bindFeatureFlagsRepository(
        impl: FeatureFlagsRepositoryImpl
    ) : FeatureFlagsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigProvideModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        return FirebaseRemoteConfig.getInstance()
    }

    @Provides
    @Singleton
    fun provideRemoteConfigDataSource(
        firebaseRemoteConfig: FirebaseRemoteConfig
    ): RemoteConfigDataSource {
        return RemoteConfigDataSource(firebaseRemoteConfig)
    }
}