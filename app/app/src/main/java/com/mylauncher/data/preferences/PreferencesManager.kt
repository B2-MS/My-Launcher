package com.mylauncher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mylauncher.data.model.SavedTheme
import com.mylauncher.data.model.toJsonString
import com.mylauncher.data.model.toSavedThemes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

/** Default accent: Cobalt blue (0xFF0050EF) stored as ARGB Long. */
const val DEFAULT_ACCENT_ARGB: Long = 0xFF0050EF

data class LauncherPreferences(
    val accentColorArgb: Long = DEFAULT_ACCENT_ARGB,
    val globalTileOpacity: Float = 0f,
    val tileAnimationIntervalMs: Long = 5000L,
    val bevelEnabled: Boolean = true,
    val bevelDepth: Float = 1f,
    val darkModeEnabled: Boolean = true
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ACCENT_COLOR = longPreferencesKey("accent_color_argb")
        private val KEY_TILE_OPACITY = floatPreferencesKey("tile_opacity")
        private val KEY_ANIMATION_INTERVAL = longPreferencesKey("animation_interval")
        private val KEY_SAVED_THEMES = stringPreferencesKey("saved_themes")
        private val KEY_BEVEL_ENABLED = booleanPreferencesKey("bevel_enabled")
        private val KEY_BEVEL_DEPTH = floatPreferencesKey("bevel_depth")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    }

    val preferencesFlow: Flow<LauncherPreferences> = context.dataStore.data.map { prefs ->
        LauncherPreferences(
            accentColorArgb = prefs[KEY_ACCENT_COLOR] ?: DEFAULT_ACCENT_ARGB,
            globalTileOpacity = prefs[KEY_TILE_OPACITY] ?: 0f,
            tileAnimationIntervalMs = prefs[KEY_ANIMATION_INTERVAL] ?: 5000L,
            bevelEnabled = prefs[KEY_BEVEL_ENABLED] ?: true,
            bevelDepth = prefs[KEY_BEVEL_DEPTH] ?: 1f,
            darkModeEnabled = prefs[KEY_DARK_MODE] ?: true
        )
    }

    val savedThemesFlow: Flow<List<SavedTheme>> = context.dataStore.data.map { prefs ->
        (prefs[KEY_SAVED_THEMES] ?: "").toSavedThemes()
    }

    suspend fun updateAccentColor(argb: Long) {
        context.dataStore.edit { it[KEY_ACCENT_COLOR] = argb }
    }

    suspend fun updateTileOpacity(opacity: Float) {
        context.dataStore.edit { it[KEY_TILE_OPACITY] = opacity.coerceIn(0f, 1f) }
    }

    suspend fun updateAnimationInterval(intervalMs: Long) {
        context.dataStore.edit { it[KEY_ANIMATION_INTERVAL] = intervalMs }
    }

    suspend fun updateBevelEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BEVEL_ENABLED] = enabled }
    }

    suspend fun updateBevelDepth(depth: Float) {
        context.dataStore.edit { it[KEY_BEVEL_DEPTH] = depth.coerceIn(0f, 3f) }
    }

    suspend fun updateDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun saveTheme(theme: SavedTheme) {
        context.dataStore.edit { prefs ->
            val existing = (prefs[KEY_SAVED_THEMES] ?: "").toSavedThemes().toMutableList()
            // Replace if same id, otherwise append
            val idx = existing.indexOfFirst { it.id == theme.id }
            if (idx >= 0) existing[idx] = theme else existing.add(theme)
            prefs[KEY_SAVED_THEMES] = existing.toJsonString()
        }
    }

    suspend fun deleteTheme(themeId: String) {
        context.dataStore.edit { prefs ->
            val existing = (prefs[KEY_SAVED_THEMES] ?: "").toSavedThemes().toMutableList()
            existing.removeAll { it.id == themeId }
            prefs[KEY_SAVED_THEMES] = existing.toJsonString()
        }
    }

    /** Apply a saved theme's preferences (accent, opacity, animation). */
    suspend fun applyThemePreferences(theme: SavedTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCENT_COLOR] = theme.accentColorArgb
            prefs[KEY_TILE_OPACITY] = theme.globalTileOpacity
            prefs[KEY_ANIMATION_INTERVAL] = theme.tileAnimationIntervalMs
        }
    }
}
