package com.moneyapp.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.moneyapp.presentation.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keys untuk DataStore Preferences yang digunakan oleh ThemePreferences.
 */
object ThemePreferencesKeys {
    val THEME_MODE = stringPreferencesKey("theme_mode")
}

/**
 * Mengelola preferensi tema aplikasi menggunakan DataStore.
 *
 * Menyimpan dan membaca [ThemeMode] (SYSTEM, LIGHT, DARK) secara persisten.
 * Default ke [ThemeMode.SYSTEM] jika belum pernah diset.
 */
@Singleton
class ThemePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    /**
     * Flow yang memancarkan [ThemeMode] saat ini.
     * Default ke [ThemeMode.SYSTEM] jika nilai belum tersimpan.
     */
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        val stored = prefs[ThemePreferencesKeys.THEME_MODE]
        if (stored != null) {
            try {
                ThemeMode.valueOf(stored)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        } else {
            ThemeMode.SYSTEM
        }
    }

    /**
     * Menyimpan [ThemeMode] yang dipilih ke DataStore.
     *
     * @param mode Mode tema yang akan disimpan.
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[ThemePreferencesKeys.THEME_MODE] = mode.name
        }
    }
}
