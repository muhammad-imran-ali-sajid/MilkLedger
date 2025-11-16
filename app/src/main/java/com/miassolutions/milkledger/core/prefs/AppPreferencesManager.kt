package com.miassolutions.milkledger.core.prefs


import android.content.Context
import android.content.SharedPreferences
import com.miassolutions.milkledger.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Manages all application-level preferences, such as theme and background color.
 * This class isolates SharedPreferences logic from UI components.
 */
@Singleton // Ensures only one instance of this manager exists across the app
class AppPreferencesManager @Inject constructor(@ApplicationContext context: Context) { // Hilt constructor injection

    // SharedPreferences constants
    private val PREFS_NAME = "AppBackgroundPrefs"
    private val KEY_BG_COLOR = "BackgroundColorKey"

    // Default color to use if no preference is found (System color)
    val DEFAULT_COLOR_ID = R.color.primary

    // Initialize SharedPreferences instance
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Retrieves the saved color resource ID from SharedPreferences.
     * @return The saved color resource ID, or DEFAULT_COLOR_ID if none is found.
     */
    fun loadBackgroundColor(): Int {
        return sharedPrefs.getInt(KEY_BG_COLOR, DEFAULT_COLOR_ID)
    }

    /**
     * Saves the chosen color resource ID to SharedPreferences.
     * @param colorId The resource ID (e.g., R.color.blue) of the selected background color.
     */
    fun saveBackgroundColor(colorId: Int) {
        sharedPrefs.edit {
            putInt(KEY_BG_COLOR, colorId)
            // Use apply() for asynchronous saving
        }
    }
}