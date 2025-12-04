package com.miassolutions.milkledger.core.di

// CoroutinesQualifiers.kt
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
