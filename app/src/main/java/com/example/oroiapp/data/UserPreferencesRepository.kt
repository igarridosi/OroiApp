package com.example.oroiapp.data

import android.content.Context
import android.content.SharedPreferences

// Erabiltzaileak aukera ditzakeen gaiak definitzeko Enum bat
enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(IS_FIRST_LAUNCH_KEY, true)
    }

    fun getUsername(): String {
        return prefs.getString(USERNAME_KEY, "") ?: ""
    }

    fun saveUsername(name: String) {
        prefs.edit()
            .putString(USERNAME_KEY, name)
            .putBoolean(IS_FIRST_LAUNCH_KEY, false)
            .apply()
    }

    /**
     * Erabiltzaileak aukeratutako gaia gordetzen du.
     */
    fun saveThemeSetting(theme: ThemeSetting) {
        prefs.edit().putString(THEME_SETTING_KEY, theme.name).apply()
    }

    /**
     * Unean gordeta dagoen gaia irakurtzen du.
     * Lehenetsitako balioa 'SYSTEM' da.
     */
    fun getThemeSetting(): ThemeSetting {
        val themeName = prefs.getString(THEME_SETTING_KEY, ThemeSetting.SYSTEM.name)
        return ThemeSetting.valueOf(themeName ?: ThemeSetting.SYSTEM.name)
    }

    companion object {
        private const val PREFS_NAME = "OroiUserPrefs"
        private const val USERNAME_KEY = "username"
        private const val IS_FIRST_LAUNCH_KEY = "is_first_launch"
        // Gako berria gaia gordetzeko
        private const val THEME_SETTING_KEY = "theme_setting"
    }
}