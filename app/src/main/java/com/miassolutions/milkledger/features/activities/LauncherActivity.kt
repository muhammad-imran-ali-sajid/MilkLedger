package com.miassolutions.milkledger.features.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.core.ui.BaseActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LauncherActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)



        lifecycleScope.launch {

            delay(2000)
            val currentUser = FirebaseAuth.getInstance().currentUser
            val target =
                if (currentUser != null) MainActivity::class.java else LoginActivity::class.java

            startActivity(Intent(this@LauncherActivity, target))
            finish()
        }


    }
}



