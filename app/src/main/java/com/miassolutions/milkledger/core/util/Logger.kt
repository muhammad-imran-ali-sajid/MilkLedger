package com.miassolutions.milkledger.core.util


import android.util.Log

/*
* With Class Auto Tag:
Logger.d<MainActivity>("Activity started")
Logger.e<NetworkManager>("Network failed")

* With Custom Tag:
Logger.d("Fetching data", "DataRepo")


* With Default Tag:
Logger.d("Debug message")
Logger.e("Error occurred")


* */

object Logger {

    // Toggle this to enable/disable logging globally
    var isDebug = true

    // Default tag if none is provided
    private const val DEFAULT_TAG = "AppLogger"

    fun d(message: String, tag: String = DEFAULT_TAG) {
        if (isDebug) Log.d(tag, message)
    }

    fun e(message: String, tag: String = DEFAULT_TAG) {
        if (isDebug) Log.e(tag, message)
    }

    fun i(message: String, tag: String = DEFAULT_TAG) {
        if (isDebug) Log.i(tag, message)
    }

    fun w(message: String, tag: String = DEFAULT_TAG) {
        if (isDebug) Log.w(tag, message)
    }

    fun v(message: String, tag: String = DEFAULT_TAG) {
        if (isDebug) Log.v(tag, message)
    }

    // Log with class name automatically as tag
    inline fun <reified T> d(message: String) {
        if (isDebug) Log.d(T::class.java.simpleName, message)
    }

    inline fun <reified T> e(message: String) {
        if (isDebug) Log.e(T::class.java.simpleName, message)
    }

    // Add more as needed...
}
