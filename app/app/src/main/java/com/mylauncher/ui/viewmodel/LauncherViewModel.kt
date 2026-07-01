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
import com.mylauncher.service.LiveTileInfo
import com.mylauncher.service.NotificationService
import com.mylauncher.widget.LauncherWidgetHost
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LauncherUiState(
    val tiles: List<Tile> = emptyList(),
    val apps: List<AppInfo> = emptyList(),
    val preferences: LauncherPreferences = LauncherPreferences(),
    val expandedGroups: Set<String> = emptySet(),
    val savedThemes: List<SavedTheme> = emptyList(),
    val liveTileData: Map<String, LiveTileInfo> = emptyMap(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class LauncherViewModel @Inject constructor(
    application: Application,
    private val appRepository: AppRepository,
    private val tileRepository: TileRepository,
    private val preferencesManager: PreferencesManager,
    val widgetHost: LauncherWidgetHost
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

        // Observe expanded groups
        viewModelScope.launch {
            tileRepository.expandedGroups.collect { expanded ->
                _uiState.update { it.copy(expandedGroups = expanded) }
            }
        }

        // Observe saved themes
        viewModelScope.launch {
            preferencesManager.savedThemesFlow.collect { themes ->
                _uiState.update { it.copy(savedThemes = themes) }
            }
        }

        // Observe live tile notification data
        viewModelScope.launch {
            NotificationService.liveTileData.collect { data ->
                _uiState.update { it.copy(liveTileData = data) }
            }
        }

        // Load apps and create default tiles
        viewModelScope.launch {
            loadApps()
        }
    }

    private suspend fun loadApps() {
        val apps = withContext(Dispatchers.IO) {
            appRepository.getInstalledApps()
        }
        _uiState.update { it.copy(apps = apps, isLoading = false) }

        // If no tiles exist yet, seed with some defaults from installed apps
        if (tileRepository.tiles.value.isEmpty() && apps.isNotEmpty()) {
            val defaultTiles = apps
                .filter { it.packageName != "com.mylauncher" }
                .take(12)
                .mapIndexed { index, app ->
                val col = (index % 3) * 2   // 0, 2, 4, 0, 2, 4, ...
                val row = (index / 3) * 2   // 0, 0, 0, 2, 2, 2, ...
                Tile(
                    packageName = app.packageName,
                    appName = app.appName,
                    columnSpan = 2,
                    rowSpan = 2,
                    position = index,
                    gridCol = col,
                    gridRow = row
                )
            }
            tileRepository.setTiles(defaultTiles)
        }
    }

    fun refreshApps() {
        viewModelScope.launch { loadApps() }
    }

    fun launchApp(packageName: String, userSerialNumber: Long = 0L) {
        appRepository.launchApp(packageName, userSerialNumber)
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun exitEditMode() {
        tileRepository.compactGrid()
        _uiState.update { it.copy(isEditMode = false) }
    }

    fun pinApp(appInfo: AppInfo) {
        tileRepository.addTile(
            Tile(
                packageName = appInfo.packageName,
                appName = appInfo.appName,
                columnSpan = 2,
                rowSpan = 2,
                userSerialNumber = appInfo.userSerialNumber
            )
        )
    }

    fun unpinTile(tileId: String) {
        val tile = _uiState.value.tiles.find { it.id == tileId }
        if (tile != null && tile.isWidget) {
            widgetHost.deleteWidgetId(tile.appWidgetId)
        }
        tileRepository.removeTile(tileId)
    }

    fun setTileSpans(tileId: String, columnSpan: Int, rowSpan: Int) {
        tileRepository.setTileSpans(tileId, columnSpan, rowSpan)
    }

    fun toggleLiveTile(tileId: String) {
        tileRepository.toggleLiveTile(tileId)
    }

    fun swapTiles(tileId1: String, tileId2: String) {
        tileRepository.swapTiles(tileId1, tileId2)
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        tileRepository.moveTile(fromIndex, toIndex)
    }

    fun moveTileToGrid(tileId: String, col: Int, row: Int) {
        tileRepository.moveTileToGrid(tileId, col, row)
    }

    fun moveGroupToGrid(groupId: String, col: Int, row: Int) {
        tileRepository.moveGroupToGrid(groupId, col, row)
    }

    fun updateAccentColor(argb: Long) {
        viewModelScope.launch { preferencesManager.updateAccentColor(argb) }
    }

    fun updateTileOpacity(opacity: Float) {
        viewModelScope.launch { preferencesManager.updateTileOpacity(opacity) }
    }

    fun updateAnimationInterval(intervalMs: Long) {
        viewModelScope.launch { preferencesManager.updateAnimationInterval(intervalMs) }
    }

    fun updateBevelEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.updateBevelEnabled(enabled) }
    }

    fun updateBevelDepth(depth: Float) {
        viewModelScope.launch { preferencesManager.updateBevelDepth(depth) }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.updateDarkMode(enabled) }
    }

    fun updateWallpaperOnlyInTiles(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.updateWallpaperOnlyInTiles(enabled) }
    }

    fun updateGridColumns(columns: Int) {
        tileRepository.setGridColumns(columns)
    }

    fun createGroup(tileId1: String, tileId2: String) {
        tileRepository.createGroup(tileId1, tileId2)
    }

    fun addToGroup(tileId: String, groupId: String) {
        tileRepository.addToGroup(tileId, groupId)
    }

    fun renameGroup(groupId: String, newName: String) {
        tileRepository.renameGroup(groupId, newName)
    }

    fun setGroupSpans(groupId: String, colSpan: Int, rowSpan: Int) {
        tileRepository.setGroupSpans(groupId, colSpan, rowSpan)
    }

    fun ungroupTile(tileId: String) {
        tileRepository.ungroupTile(tileId)
    }

    fun toggleGroupExpanded(groupId: String) {
        tileRepository.toggleGroupExpanded(groupId)
    }

    fun swapGroupTiles(tileId1: String, tileId2: String) {
        tileRepository.swapGroupTiles(tileId1, tileId2)
    }

    fun moveGroupTile(tileId: String, targetCol: Int, targetRow: Int) {
        tileRepository.moveGroupTile(tileId, targetCol, targetRow)
    }

    // ─────────────── Widget operations ───────────────

    /** Allocate a new widget ID for the picker flow. */
    fun allocateWidgetId(): Int = widgetHost.allocateWidgetId()

    /** Add a bound widget to the tile grid. */
    fun addWidget(appWidgetId: Int, label: String, columnSpan: Int = 4, rowSpan: Int = 2) {
        val info = widgetHost.getWidgetInfo(appWidgetId)
        val packageName = info?.provider?.packageName ?: "widget"
        tileRepository.addTile(
            Tile(
                packageName = packageName,
                appName = label,
                columnSpan = columnSpan.coerceIn(1, 6),
                rowSpan = rowSpan.coerceIn(1, 4),
                appWidgetId = appWidgetId
            )
        )
    }

    /** Remove a widget tile and delete its widget ID. */
    fun removeWidget(tileId: String) {
        val tile = _uiState.value.tiles.find { it.id == tileId }
        if (tile != null && tile.isWidget) {
            widgetHost.deleteWidgetId(tile.appWidgetId)
        }
        tileRepository.removeTile(tileId)
    }

    // ─────────────── Theme operations ───────────────

    fun saveCurrentAsTheme(name: String) {
        viewModelScope.launch {
            val state = _uiState.value
            val theme = SavedTheme(
                id = UUID.randomUUID().toString(),
                name = name,
                accentColorArgb = state.preferences.accentColorArgb,
                globalTileOpacity = state.preferences.globalTileOpacity,
                tileAnimationIntervalMs = state.preferences.tileAnimationIntervalMs,
                bevelEnabled = state.preferences.bevelEnabled,
                bevelDepth = state.preferences.bevelDepth,
                darkModeEnabled = state.preferences.darkModeEnabled,
                wallpaperOnlyInTiles = state.preferences.wallpaperOnlyInTiles,
                tiles = state.tiles.map { tile ->
                    TileSnapshot(
                        packageName = tile.packageName,
                        appName = tile.appName,
                        columnSpan = tile.columnSpan,
                        rowSpan = tile.rowSpan,
                        position = tile.position,
                        colorOverride = tile.colorOverride,
                        transparencyOverride = tile.transparencyOverride,
                        isLiveTile = tile.isLiveTile,
                        groupId = tile.groupId,
                        groupName = tile.groupName,
                        groupCol = tile.groupCol,
                        groupRow = tile.groupRow,
                        gridCol = tile.gridCol,
                        gridRow = tile.gridRow,
                        groupHeaderColSpan = tile.groupHeaderColSpan,
                        groupHeaderRowSpan = tile.groupHeaderRowSpan,
                        userSerialNumber = tile.userSerialNumber
                    )
                }
            )
            preferencesManager.saveTheme(theme)
        }
    }

    fun applyTheme(theme: SavedTheme) {
        viewModelScope.launch {
            // Apply all preferences (accent, opacity, animation, bevel, dark mode, wallpaper)
            preferencesManager.applyThemePreferences(theme)

            // Rebuild tile list from theme snapshots
            val restoredTiles = theme.tiles.map { snap ->
                Tile(
                    packageName = snap.packageName,
                    appName = snap.appName,
                    columnSpan = snap.columnSpan,
                    rowSpan = snap.rowSpan,
                    position = snap.position,
                    colorOverride = snap.colorOverride,
                    transparencyOverride = snap.transparencyOverride,
                    isLiveTile = snap.isLiveTile,
                    groupId = snap.groupId,
                    groupName = snap.groupName,
                    groupCol = snap.groupCol,
                    groupRow = snap.groupRow,
                    gridCol = snap.gridCol,
                    gridRow = snap.gridRow,
                    groupHeaderColSpan = snap.groupHeaderColSpan,
                    groupHeaderRowSpan = snap.groupHeaderRowSpan,
                    userSerialNumber = snap.userSerialNumber
                )
            }
            tileRepository.setTiles(restoredTiles)
        }
    }

    fun deleteTheme(themeId: String) {
        viewModelScope.launch {
            preferencesManager.deleteTheme(themeId)
        }
    }
}
