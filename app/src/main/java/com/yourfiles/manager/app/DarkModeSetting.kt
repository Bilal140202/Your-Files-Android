package com.yourfiles.manager.app

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Centralised dark mode preference holder.
 *
 * Uses a Compose [mutableStateOf] so that any Composable reading [isDarkMode]
 * will automatically recompose when the value changes.
 *
 * Simple Day/Night toggle — no "system" option.
 */
object DarkModeSetting {

    private const val PREFS_NAME = "yourfiles_settings"
    private const val KEY_DARK_MODE = "dark_mode"

    /** Current preference value. Read this inside a Composable to trigger recomposition. */
    val isDarkMode = mutableStateOf(false)

    /** Load the persisted value from SharedPreferences. Call once at app start. */
    fun load(context: Context) {
        isDarkMode.value = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)
    }

    /** Toggle between light and dark. Persists immediately. */
    fun toggle(context: Context) {
        val next = !isDarkMode.value
        isDarkMode.value = next
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, next)
            .apply()
    }
}