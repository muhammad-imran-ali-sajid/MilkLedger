package com.miassolutions.milkledger.core.managers



import android.content.Context
import androidx.core.content.edit

class PasswordManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "milkledger_prefs"
        private const val KEY_PASSWORD = "password_key"
    }

    private val sharedPref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isPasswordSet(): Boolean = sharedPref.contains(KEY_PASSWORD)

    fun savePassword(password: String) {
        sharedPref.edit {
            putString(KEY_PASSWORD, password)
        }
    }

    fun isPasswordCorrect(input: String): Boolean {
        val savedPassword = sharedPref.getString(KEY_PASSWORD, null)
        return savedPassword != null && input == savedPassword
    }

    fun clearPassword() {
        sharedPref.edit {
            remove(KEY_PASSWORD)
        }
    }
}
