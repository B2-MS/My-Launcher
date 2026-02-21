package com.mylauncher.ui.navigation

import android.widget.Toast
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
                        isEditMode = uiState.isEditMode,
                        expandedGroups = uiState.expandedGroups,
                        onTileTap = { packageName -> viewModel.launchApp(packageName) },
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
                        onNavigateToAppList = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                        onExitEditMode = { viewModel.exitEditMode() }
                    )
                    1 -> AppListScreen(
                        apps = uiState.apps,
                        accentColor = accentColor,
                        darkModeEnabled = uiState.preferences.darkModeEnabled,
                        onAppTap = { packageName ->
                            viewModel.launchApp(packageName)
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
                savedThemes = uiState.savedThemes,
                onAccentColorChanged = { viewModel.updateAccentColor(it) },
                onTileOpacityChanged = { viewModel.updateTileOpacity(it) },
                onAnimationIntervalChanged = { viewModel.updateAnimationInterval(it) },
                onBevelEnabledChanged = { viewModel.updateBevelEnabled(it) },
                onBevelDepthChanged = { viewModel.updateBevelDepth(it) },
                onDarkModeChanged = { viewModel.updateDarkMode(it) },
                onSaveTheme = { name -> viewModel.saveCurrentAsTheme(name) },
                onApplyTheme = { theme -> viewModel.applyTheme(theme) },
                onDeleteTheme = { themeId -> viewModel.deleteTheme(themeId) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
