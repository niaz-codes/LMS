package com.example.uos_lms.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromStringOrNull(raw: String?): ThemeMode? = entries.find { it.name == raw }
    }
}

private object ThemeKeys {
    val MODE = stringPreferencesKey("theme_mode")
}

@Singleton
class ThemePreferenceManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromStringOrNull(prefs[ThemeKeys.MODE]) ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[ThemeKeys.MODE] = mode.name }
    }
}
