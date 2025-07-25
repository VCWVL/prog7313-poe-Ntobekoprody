package com.example.spendly.data

import android.content.Context

object ThemePreferenceManager {
    private const val PREF_NAME = "user_prefs"
    private const val THEME_KEY = "dark_mode"

    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(THEME_KEY, false)
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(THEME_KEY, enabled).apply()
    }
}
