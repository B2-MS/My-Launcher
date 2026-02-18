package com.mylauncher.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mylauncher.data.model.*
import com.mylauncher.data.preferences.LauncherPreferences
import com.mylauncher.data.preferences.PreferencesManager
import com.mylauncher.data.repository.AppRepository
import com.mylauncher.data.repository.TileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LauncherUiState(
    val tiles: List<Tile> = emptyList(),
    val apps: List<AppInfo> = emptyList(),
    val preferences: LauncherPreferences = LauncherPreferences(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LauncherViewModel @Inject constructor(
    application: Application,
    private val appRepository: AppRepository,
    private val tileRepository: TileRepository,
    private val preferencesManager: PreferencesManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        // Observe preferences
        viewModelScope.launch {
            preferencesManager.preferencesFlow.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }

        // Observe tiles
        viewModelScope.launch {
            tileRepository.tiles.collect { tiles ->
                _uiState.update { it.copy(tiles = tiles) }
            }
        }

        // Load apps and create default tiles
        viewModelScope.launch {
            loadApps()
        }
    }

    private fun loadApps() {
        val apps = appRepository.getInstalledApps()
        _uiState.update { it.copy(apps = apps, isLoading = false) }

        // If no tiles exist yet, seed with some defaults from installed apps
        if (tileRepository.tiles.value.isEmpty() && apps.isNotEmpty()) {
            val defaultTiles = apps.take(12).mapIndexed { index, app ->
                Tile(
                    packageName = app.packageName,
                    appName = app.appName,
                    size = when {
                        index == 0 -> TileSize.WIDE
                        index % 4 == 0 -> TileSize.SMALL
                        else -> TileSize.MEDIUM
                    },
                    position = index
                )
            }
            tileRepository.setTiles(defaultTiles)
        }
    }

    fun refreshApps() {
        viewModelScope.launch { loadApps() }
    }

    fun launchApp(packageName: String) {
        val intent = appRepository.getLaunchIntent(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            getApplication<Application>().startActivity(intent)
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun exitEditMode() {
        _uiState.update { it.copy(isEditMode = false) }
    }

    fun pinApp(appInfo: AppInfo) {
        tileRepository.addTile(
            Tile(
                packageName = appInfo.packageName,
                appName = appInfo.appName,
                size = TileSize.MEDIUM
            )
        )
    }

    fun unpinTile(tileId: String) {
        tileRepository.removeTile(tileId)
    }

    fun resizeTile(tileId: String) {
        tileRepository.resizeTile(tileId)
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        tileRepository.moveTile(fromIndex, toIndex)
    }

    fun updateAccentColor(color: AccentColor) {
        viewModelScope.launch { preferencesManager.updateAccentColor(color) }
    }

    fun updateDarkTheme(isDark: Boolean) {
        viewModelScope.launch { preferencesManager.updateDarkTheme(isDark) }
    }

    fun updateGridColumns(columns: Int) {
        viewModelScope.launch { preferencesManager.updateGridColumns(columns) }
    }

    fun updateTileOpacity(opacity: Float) {
        viewModelScope.launch { preferencesManager.updateTileOpacity(opacity) }
    }

    fun updateAnimationInterval(intervalMs: Long) {
        viewModelScope.launch { preferencesManager.updateAnimationInterval(intervalMs) }
    }
}
