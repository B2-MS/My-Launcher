package com.mylauncher.ui.navigation

import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mylauncher.data.model.toComposeColor
import com.mylauncher.ui.screens.AppListScreen
import com.mylauncher.ui.screens.SettingsScreen
import com.mylauncher.ui.screens.StartScreen
import com.mylauncher.ui.screens.WidgetPickerScreen
import com.mylauncher.ui.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

@Composable
fun LauncherNavHost(
    viewModel: LauncherViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val accentColor = uiState.preferences.accentColorArgb.toComposeColor()

    // Wallpaper picker launcher
    val wallpaperLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* user returned from wallpaper picker */ }

    // Widget bind permission launcher — used when AppWidgetManager requires explicit bind
    var pendingWidgetId by remember { mutableIntStateOf(0) }
    var pendingWidgetLabel by remember { mutableStateOf("") }
    val widgetBindLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && pendingWidgetId != 0) {
            viewModel.addWidget(pendingWidgetId, pendingWidgetLabel)
            navController.popBackStack(Routes.START_SCREEN, inclusive = false)
        } else if (pendingWidgetId != 0) {
            viewModel.widgetHost.deleteWidgetId(pendingWidgetId)
        }
        pendingWidgetId = 0
        pendingWidgetLabel = ""
    }

    NavHost(
        navController = navController,
        startDestination = Routes.START_SCREEN,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Start Screen + App List live in a HorizontalPager (swipe right/left)
        composable(Routes.START_SCREEN) {
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1
            ) { page ->
                when (page) {
                    0 -> StartScreen(
                        tiles = uiState.tiles,
                        apps = uiState.apps,
                        accentColor = accentColor,
                        tileOpacity = uiState.preferences.globalTileOpacity,
                        bevelEnabled = uiState.preferences.bevelEnabled,
                        bevelDepth = uiState.preferences.bevelDepth,
                        wallpaperOnlyInTiles = uiState.preferences.wallpaperOnlyInTiles,
                        isEditMode = uiState.isEditMode,
                        expandedGroups = uiState.expandedGroups,
                        onTileTap = { packageName, serial -> viewModel.launchApp(packageName, serial) },
                        onTileLongPress = { viewModel.toggleEditMode() },
                        onUnpinTile = { tileId -> viewModel.unpinTile(tileId) },
                        onSetTileSpans = { tileId, colSpan, rowSpan -> viewModel.setTileSpans(tileId, colSpan, rowSpan) },
                        onToggleLiveTile = { tileId -> viewModel.toggleLiveTile(tileId) },
                        onSwapTiles = { fromId, toId -> viewModel.swapTiles(fromId, toId) },
                        onCreateGroup = { id1, id2 -> viewModel.createGroup(id1, id2) },
                        onAddToGroup = { tileId, groupId -> viewModel.addToGroup(tileId, groupId) },
                        onUngroupTile = { tileId -> viewModel.ungroupTile(tileId) },
                        onSwapGroupTiles = { id1, id2 -> viewModel.swapGroupTiles(id1, id2) },
                        onMoveGroupTile = { tileId, col, row -> viewModel.moveGroupTile(tileId, col, row) },
                        onToggleGroupExpanded = { groupId -> viewModel.toggleGroupExpanded(groupId) },
                        onMoveTileToGrid = { tileId, col, row -> viewModel.moveTileToGrid(tileId, col, row) },
                        onMoveGroupToGrid = { groupId, col, row -> viewModel.moveGroupToGrid(groupId, col, row) },
                        onRenameGroup = { groupId, newName -> viewModel.renameGroup(groupId, newName) },
                        onSetGroupSpans = { groupId, colSpan, rowSpan -> viewModel.setGroupSpans(groupId, colSpan, rowSpan) },
                        onNavigateToAppList = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                        onExitEditMode = { viewModel.exitEditMode() },
                        onChangeWallpaper = {
                            val intent = Intent(WallpaperManager.ACTION_CROP_AND_SET_WALLPAPER)
                            try {
                                wallpaperLauncher.launch(intent)
                            } catch (_: Exception) {
                                // Fallback: open live wallpaper picker or system wallpaper chooser
                                try {
                                    wallpaperLauncher.launch(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "No wallpaper picker available", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onAddWidget = {
                            navController.navigate(Routes.WIDGET_PICKER)
                        },
                        onUpdateGridColumns = { viewModel.updateGridColumns(it) },
                        liveTileData = uiState.liveTileData,
                        widgetHost = viewModel.widgetHost
                    )
                    1 -> AppListScreen(
                        apps = uiState.apps,
                        accentColor = accentColor,
                        darkModeEnabled = uiState.preferences.darkModeEnabled,
                        onAppTap = { packageName, serial ->
                            viewModel.launchApp(packageName, serial)
                        },
                        onAppLongPress = { appInfo ->
                            viewModel.pinApp(appInfo)
                            Toast.makeText(context, "${appInfo.appName} pinned", Toast.LENGTH_SHORT).show()
                        },
                        onNavigateBack = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                }
            }
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                currentAccentColor = accentColor,
                accentColorArgb = uiState.preferences.accentColorArgb,
                tileOpacity = uiState.preferences.globalTileOpacity,
                animationIntervalMs = uiState.preferences.tileAnimationIntervalMs,
                bevelEnabled = uiState.preferences.bevelEnabled,
                bevelDepth = uiState.preferences.bevelDepth,
                darkModeEnabled = uiState.preferences.darkModeEnabled,
                wallpaperOnlyInTiles = uiState.preferences.wallpaperOnlyInTiles,
                savedThemes = uiState.savedThemes,
                onAccentColorChanged = { viewModel.updateAccentColor(it) },
                onTileOpacityChanged = { viewModel.updateTileOpacity(it) },
                onAnimationIntervalChanged = { viewModel.updateAnimationInterval(it) },
                onBevelEnabledChanged = { viewModel.updateBevelEnabled(it) },
                onBevelDepthChanged = { viewModel.updateBevelDepth(it) },
                onDarkModeChanged = { viewModel.updateDarkMode(it) },
                onWallpaperOnlyInTilesChanged = { viewModel.updateWallpaperOnlyInTiles(it) },
                onSaveTheme = { name -> viewModel.saveCurrentAsTheme(name) },
                onApplyTheme = { theme -> viewModel.applyTheme(theme) },
                onDeleteTheme = { themeId -> viewModel.deleteTheme(themeId) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Widget picker screen
        composable(Routes.WIDGET_PICKER) {
            val providers = remember { viewModel.widgetHost.getInstalledProviders() }
            WidgetPickerScreen(
                providers = providers,
                accentColor = accentColor,
                onWidgetSelected = { info ->
                    val widgetId = viewModel.allocateWidgetId()
                    val appWidgetManager = AppWidgetManager.getInstance(context)

                    // Try to bind the widget directly
                    val bound = appWidgetManager.bindAppWidgetIdIfAllowed(
                        widgetId, info.provider
                    )
                    if (bound) {
                        val label = info.loadLabel(context.packageManager)?.toString() ?: "Widget"
                        // Calculate reasonable span from widget min size
                        val cellWidth = 60  // approximate dp per grid cell
                        val colSpan = ((info.minWidth / cellWidth) + 1).coerceIn(2, 6)
                        val rowSpan = ((info.minHeight / cellWidth) + 1).coerceIn(1, 4)
                        viewModel.addWidget(widgetId, label, colSpan, rowSpan)
                        navController.popBackStack(Routes.START_SCREEN, inclusive = false)
                    } else {
                        // Need explicit permission — launch bind intent
                        pendingWidgetId = widgetId
                        pendingWidgetLabel = info.loadLabel(context.packageManager)?.toString() ?: "Widget"
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
                        }
                        widgetBindLauncher.launch(intent)
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
