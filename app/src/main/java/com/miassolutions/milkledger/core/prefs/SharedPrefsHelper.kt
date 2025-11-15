package com.miassolutions.milkledger.core.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefsHelper {

    private const val PREFS_NAME = "milkledger_prefs"
    private const val KEY_ROLE = "user_role"
    private const val KEY_MAIL = "user_mail"

    private const val KEY_EDIT_LOCK = "key_edit_lock"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // --- Role Constants ---
    const val ROLE_ADMIN = "admin"

    const val ROLE_EMPLOYER = "employer"
    const val ROLE_USER = "user" // default fallback

    const val EDIT_LOCK = "edit_lock"

    // --- Save ---



    fun saveUserRole(context: Context, role: String) {
        prefs(context).edit(commit = true) { // commit ensures it's immediately saved
            putString(KEY_ROLE, role)
        }
    }

    fun saveUserMail(context: Context, email: String) {
        prefs(context).edit(commit = true) {
            putString(KEY_MAIL, email)
        }
    }

    fun getUserMail(context: Context): String {
        return prefs(context).getString(KEY_MAIL, "no mail") ?: ""
    }

    // --- Get ---
    fun getUserRole(context: Context): String {
        return prefs(context).getString(KEY_ROLE, ROLE_USER) ?: ROLE_USER
    }

    // --- Check Role ---
    fun isAdmin(context: Context): Boolean = getUserRole(context) == ROLE_ADMIN
    fun isEmployer(context: Context): Boolean = getUserRole(context) == ROLE_EMPLOYER

    // --- Clear ---
    fun clearUserRole(context: Context) {
        prefs(context).edit(commit = true) { remove(KEY_ROLE) }
    }
}
