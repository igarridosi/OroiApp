package com.example.oroiapp.data

import android.content.Context
import android.content.SharedPreferences

// Erabiltzaileak aukera ditzakeen gaiak definitzeko Enum bat
enum class ThemeSetting {
    SYSTEM, LIGHT, DARK
}

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val BUDGET_KEY = "monthly_budget"

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
    /**
     * Gordetako aurrekontua irakurtzen du.
     * Lehenetsitako balioa 0.0 da (mugarik gabe).
     */
    fun getMonthlyBudget(): Double {
        // String bezala gorde eta Double-era bihurtu, zehaztasuna ez galtzeko
        val budgetString = prefs.getString(BUDGET_KEY, "0.0")
        return budgetString?.toDoubleOrNull() ?: 0.0
    }
    /**
     * Aurrekontu berria gordetzen du.
     */
    fun saveMonthlyBudget(budget: Double) {
        prefs.edit().putString(BUDGET_KEY, budget.toString()).apply()
    }

    fun saveLanguageTag(tag: String) {
        prefs.edit().putString(LANGUAGE_TAG_KEY, tag).apply()
    }

    fun getLanguageTag(): String = prefs.getString(LANGUAGE_TAG_KEY, "") ?: ""

    companion object {
        const val PREFS_NAME = "OroiUserPrefs"
        private const val USERNAME_KEY = "username"
        private const val IS_FIRST_LAUNCH_KEY = "is_first_launch"
        private const val THEME_SETTING_KEY = "theme_setting"
        private const val BUDGET_KEY = "monthly_budget"
        const val LANGUAGE_TAG_KEY = "language_tag"
    }
}