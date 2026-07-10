package com.yourfiles.manager.app

import android.content.Context
import androidx.compose.runtime.mutableStateOf

/**
 * Centralised dark mode preference holder.
 *
 * Uses a Compose [mutableStateOf] so that any Composable reading [currentSetting]
 * will automatically recompose when the value changes.
 *
 * Values: `"system"` (follow device setting), `"dark"`, `"light"`.
 */
object DarkModeSetting {

    private const val PREFS_NAME = "yourfiles_settings"
    private const val KEY_DARK_MODE = "dark_mode"

    /** Current preference value. Read this inside a Composable to trigger recomposition. */
    val currentSetting = mutableStateOf("system")

    /** Load the persisted value from SharedPreferences. Call once at app start. */
    fun load(context: Context) {
        currentSetting.value = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_DARK_MODE, "system") ?: "system"
    }

    /**
     * Cycle through the three states: system → dark → light → system.
     * Persists the new value to SharedPreferences and updates [currentSetting].
     */
    fun cycle(context: Context) {
        val next = when (currentSetting.value) {
            "system" -> "dark"
            "dark"   -> "light"
            else     -> "system"
        }
        currentSetting.value = next
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_DARK_MODE, next)
            .apply()
    }
}
