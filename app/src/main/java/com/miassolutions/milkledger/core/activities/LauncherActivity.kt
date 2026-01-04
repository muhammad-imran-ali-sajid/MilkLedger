package com.miassolutions.milkledger.core.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.features.owner.AppStartViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    private val viewModel: AppStartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        lifecycleScope.launch {
            viewModel.startDestination.collectLatest { destinationId ->
                // Go to LoginActivity if no Firebase user
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    startActivity(Intent(this@LauncherActivity, LoginActivity::class.java))
                } else {
                    // Otherwise go to MainActivity with the start destination
                    val intent = Intent(this@LauncherActivity, MainActivity::class.java)
                    intent.putExtra("startDestination", destinationId)
                    startActivity(intent)
                }
                finish()
            }
        }

//        Handler(Looper.getMainLooper()).postDelayed({
//            val currentUser = FirebaseAuth.getInstance().currentUser
//            val target = if (currentUser != null) MainActivity::class.java else LoginActivity::class.java
//
//            startActivity(Intent(this, target))
//            finish()
//        },2500L)


    }
}



