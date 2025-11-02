package com.miassolutions.milkledger

import android.app.Application
import com.google.firebase.FirebaseApp
import com.miassolutions.milkledger.core.helper.RemoteConfigHelper
import com.miassolutions.milkledger.data.repository.CustomerRepository
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MilkLedgerApp : Application() {

    @Inject
    lateinit var customerRepository: CustomerRepository

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        RemoteConfigHelper.init()

        // Trigger Firestore sync after launch
        CoroutineScope(Dispatchers.IO).launch {
            customerRepository.refreshFromFirestore() // 🔽 Fetch from Firestore

        }
    }
}