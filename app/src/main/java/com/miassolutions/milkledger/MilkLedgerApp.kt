package com.miassolutions.milkledger

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.miassolutions.milkledger.core.feature.FeatureManager
import com.miassolutions.milkledger.core.helper.RemoteConfigHelper
import com.miassolutions.milkledger.data.repository.CustomerRepository
import com.miassolutions.milkledger.data.repository.DataRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MilkLedgerApp : Application() {


    @Inject
    lateinit var dataRepository: DataRepository

    @Inject
    lateinit var featureManager: FeatureManager

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        RemoteConfigHelper.init()

        CoroutineScope(Dispatchers.IO).launch {
            featureManager.refreshFlags()
        }


//        dataRepository.initialize()


        DynamicColors.applyToActivitiesIfAvailable(this)

    }
}