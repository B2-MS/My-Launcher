package com.mylauncher.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mylauncher.data.model.AccentColor
import com.mylauncher.data.model.AppInfo
import com.mylauncher.data.model.Tile
import com.mylauncher.data.model.TileSize
import com.mylauncher.ui.components.TileItem
import androidx.compose.material.icons.filled.Settings

/**
 * The main Start Screen — a vertically scrolling Metro-style tile grid.
 */
@Composable
fun StartScreen(
    tiles: List<Tile>,
    apps: List<AppInfo>,
    accentColor: AccentColor,
    isDarkTheme: Boolean,
    gridColumns: Int,
    tileOpacity: Float,
    isEditMode: Boolean,
    onTileTap: (String) -> Unit,
    onTileLongPress: () -> Unit,
    onUnpinTile: (String) -> Unit,
    onResizeTile: (String) -> Unit,
    onNavigateToAppList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onExitEditMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val horizontalPadding = 12.dp
    val gap = 4.dp
    val availableWidth = screenWidthDp - (horizontalPadding * 2)
    val columnWidth = (availableWidth - gap * (gridColumns - 1)) / gridColumns

    // Build icon lookup map
    val iconMap: Map<String, Drawable?> = remember(apps) {
        apps.associate { it.packageName to it.icon }
    }

    // Arrange tiles into rows
    val rows = remember(tiles, gridColumns) {
        arrangeTilesIntoRows(tiles, gridColumns)
    }

    val backgroundColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF5F5F5)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = 48.dp,
                bottom = 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            items(rows.size) { rowIndex ->
                val row = rows[rowIndex]
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (tile in row) {
                        TileItem(
                            tile = tile,
                            appIcon = iconMap[tile.packageName],
                            accentColor = accentColor.color,
                            tileOpacity = tileOpacity,
                            columnWidth = columnWidth,
                            gap = gap,
                            isEditMode = isEditMode,
                            onTileTap = { onTileTap(tile.packageName) },
                            onTileLongPress = onTileLongPress,
                            onUnpin = { onUnpinTile(tile.id) },
                            onResize = { onResizeTile(tile.id) }
                        )
                    }
                }
            }
        }

        // "All Apps →" button at bottom right
        AnimatedVisibility(
            visible = !isEditMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .clickable { onNavigateToAppList() }
                    .padding(8.dp)
            ) {
                Text(
                    text = "All Apps",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "All Apps",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Settings gear at bottom left
        AnimatedVisibility(
            visible = !isEditMode,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // "Done" button when in edit mode
        AnimatedVisibility(
            visible = isEditMode,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Button(
                onClick = onExitEditMode,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor.color
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Done", color = Color.White)
            }
        }
    }
}

/**
 * Arranges tiles into rows respecting column span limits.
 * Greedy row-packing: fill each row left-to-right until no more tiles fit.
 */
private fun arrangeTilesIntoRows(tiles: List<Tile>, gridColumns: Int): List<List<Tile>> {
    val rows = mutableListOf<MutableList<Tile>>()
    var currentRow = mutableListOf<Tile>()
    var currentRowSpan = 0

    for (tile in tiles) {
        val span = tile.size.columnSpan.coerceAtMost(gridColumns)
        if (currentRowSpan + span > gridColumns) {
            // Start a new row
            if (currentRow.isNotEmpty()) rows.add(currentRow)
            currentRow = mutableListOf(tile)
            currentRowSpan = span
        } else {
            currentRow.add(tile)
            currentRowSpan += span
        }
    }
    if (currentRow.isNotEmpty()) rows.add(currentRow)
    return rows
}
