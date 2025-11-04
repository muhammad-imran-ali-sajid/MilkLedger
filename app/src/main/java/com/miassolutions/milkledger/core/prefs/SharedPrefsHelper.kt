package com.miassolutions.milkledger.core.prefs


import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefsHelper {

    private const val PREFS_NAME = "milkledger_prefs"
    private const val KEY_ROLE = "user_role"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserRole(context: Context, role: String) {
        getPrefs(context).edit { putString(KEY_ROLE, role) }
    }

    fun getUserRole(context: Context): String =
        getPrefs(context).getString(KEY_ROLE, "user") ?: "user"

    fun clearUserRole(context: Context) {
        getPrefs(context).edit { remove(KEY_ROLE) }
    }
}
