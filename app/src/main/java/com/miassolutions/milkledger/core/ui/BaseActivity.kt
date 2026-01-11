package com.miassolutions.milkledger.core.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var edgeToEdge: EdgeToEdgeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        edgeToEdge = EdgeToEdgeController(this)
        edgeToEdge.enable()
    }
}