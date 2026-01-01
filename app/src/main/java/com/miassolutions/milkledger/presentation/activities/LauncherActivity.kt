package com.miassolutions.milkledger.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.miassolutions.milkledger.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            val target = if (currentUser != null) MainActivity::class.java else LoginActivity::class.java

            startActivity(Intent(this, target))
            finish()
        },2500L)


    }
}



