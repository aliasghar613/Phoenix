package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.phoenixDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "phoenix_preferences"
)

data class PhoenixSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val desktopMode: Boolean = false,
    val immersiveMode: Boolean = false,
    val openExternalLinksOutside: Boolean = true
)

class PhoenixPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DESKTOP_MODE = booleanPreferencesKey("desktop_mode")
        val IMMERSIVE_MODE = booleanPreferencesKey("immersive_mode")
        val OPEN_EXTERNAL_LINKS_OUTSIDE = booleanPreferencesKey("open_external_links_outside")
    }

    val settings: Flow<PhoenixSettings> = context.phoenixDataStore.data.map { prefs ->
        val themeName = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = ThemeMode.entries.firstOrNull { it.name == themeName } ?: ThemeMode.SYSTEM
        PhoenixSettings(
            themeMode = themeMode,
            desktopMode = prefs[Keys.DESKTOP_MODE] ?: false,
            immersiveMode = prefs[Keys.IMMERSIVE_MODE] ?: false,
            openExternalLinksOutside = prefs[Keys.OPEN_EXTERNAL_LINKS_OUTSIDE] ?: true
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.phoenixDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setDesktopMode(enabled: Boolean) {
        context.phoenixDataStore.edit { it[Keys.DESKTOP_MODE] = enabled }
    }

    suspend fun setImmersiveMode(enabled: Boolean) {
        context.phoenixDataStore.edit { it[Keys.IMMERSIVE_MODE] = enabled }
    }

    suspend fun setOpenExternalLinksOutside(enabled: Boolean) {
        context.phoenixDataStore.edit { it[Keys.OPEN_EXTERNAL_LINKS_OUTSIDE] = enabled }
    }
}
