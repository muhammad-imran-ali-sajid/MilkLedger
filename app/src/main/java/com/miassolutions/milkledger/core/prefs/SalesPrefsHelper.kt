package com.miassolutions.milkledger.core.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.time.LocalDate

/**
 * Helper object for managing SharedPreferences related to Sales/Purchases edit mode.
 */
object SalesPrefsHelper {

    private const val PREFS_NAME = "sales_purchase_prefs"
    private const val KEY_LOCKED_DATE = "edit_mode_locked_date"
    private const val KEY_LOCKED_TODAY = "edit_mode_locked_today"

    /**
     * Retrieves the SharedPreferences instance.
     */
    private fun getPrefs(context: Context): SharedPreferences {
        // Renamed from 'purchase_prefs' to a more generic name for this context
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if the edit mode has been permanently locked for the current date.
     */
    fun isEditModeLockedForToday(context: Context): Boolean {
        val prefs = getPrefs(context)
        val savedDate = prefs.getString(KEY_LOCKED_DATE, null)

        // Check 1: Does the saved lock date match today's date?
        val isSameDay = savedDate == LocalDate.now().toString()

        // Check 2: Is the 'locked' flag set to true?
        val isLocked = prefs.getBoolean(KEY_LOCKED_TODAY, false)

        // The mode is locked only if it was locked AND the locking occurred today
        return isSameDay && isLocked
    }

    /**
     * Sets or unsets the permanent lock status for edit mode for the current day.
     */
    fun setEditModeLockedForToday(context: Context, locked: Boolean) {
        getPrefs(context).edit {
            putBoolean(KEY_LOCKED_TODAY, locked)
            // Always update the date when setting the lock status to ensure daily reset logic works
            putString(KEY_LOCKED_DATE, LocalDate.now().toString())
        }
    }
}