package com.mylauncher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mylauncher.data.model.AccentColor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

data class LauncherPreferences(
    val accentColorName: String = AccentColor.COBALT.name,
    val isDarkTheme: Boolean = true,
    val gridColumns: Int = 6,
    val globalTileOpacity: Float = 0.85f,
    val tileAnimationIntervalMs: Long = 5000L
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_GRID_COLUMNS = intPreferencesKey("grid_columns")
        private val KEY_TILE_OPACITY = floatPreferencesKey("tile_opacity")
        private val KEY_ANIMATION_INTERVAL = longPreferencesKey("animation_interval")
    }

    val preferencesFlow: Flow<LauncherPreferences> = context.dataStore.data.map { prefs ->
        LauncherPreferences(
            accentColorName = prefs[KEY_ACCENT_COLOR] ?: AccentColor.COBALT.name,
            isDarkTheme = prefs[KEY_DARK_THEME] ?: true,
            gridColumns = prefs[KEY_GRID_COLUMNS] ?: 6,
            globalTileOpacity = prefs[KEY_TILE_OPACITY] ?: 0.85f,
            tileAnimationIntervalMs = prefs[KEY_ANIMATION_INTERVAL] ?: 5000L
        )
    }

    suspend fun updateAccentColor(color: AccentColor) {
        context.dataStore.edit { it[KEY_ACCENT_COLOR] = color.name }
    }

    suspend fun updateDarkTheme(isDark: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = isDark }
    }

    suspend fun updateGridColumns(columns: Int) {
        context.dataStore.edit { it[KEY_GRID_COLUMNS] = columns }
    }

    suspend fun updateTileOpacity(opacity: Float) {
        context.dataStore.edit { it[KEY_TILE_OPACITY] = opacity.coerceIn(0f, 1f) }
    }

    suspend fun updateAnimationInterval(intervalMs: Long) {
        context.dataStore.edit { it[KEY_ANIMATION_INTERVAL] = intervalMs }
    }
}
