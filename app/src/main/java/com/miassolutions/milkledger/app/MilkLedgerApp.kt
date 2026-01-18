package com.miassolutions.milkledger.app

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.miassolutions.milkledger.utils.helper.RemoteConfigHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MilkLedgerApp : Application() {






    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        RemoteConfigHelper.init()




//        dataRepository.initialize()

//
        DynamicColors.applyToActivitiesIfAvailable(this)

    }
}