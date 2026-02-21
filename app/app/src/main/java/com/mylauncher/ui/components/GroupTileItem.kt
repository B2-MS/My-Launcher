package com.mylauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import com.mylauncher.data.model.Tile
import com.mylauncher.data.model.TileGroup

/**
 * A group tile header. Shows a stacked preview of app icons when collapsed.
 * The expanded content is rendered separately by [GroupExpandedContent].
 */
@Composable
fun GroupTileItem(
    group: TileGroup,
    iconMap: Map<String, Drawable?>,
    accentColor: Color,
    tileOpacity: Float,
    bevelEnabled: Boolean,
    bevelDepth: Float,
    columnWidth: Dp,
    gap: Dp,
    isEditMode: Boolean,
    isExpanded: Boolean,
    isBeingDragged: Boolean = false,
    onToggleExpand: () -> Unit,
    onTileTap: (String) -> Unit,
    onTileEditTap: (String) -> Unit,
    onLongPress: () -> Unit,
    onUngroupTile: (String) -> Unit,
    onSwapGroupTiles: (String, String) -> Unit,
    onGroupEditTap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tileWidth = columnWidth * group.columnSpan + gap * (group.columnSpan - 1)
    val tileHeight = columnWidth * group.rowSpan + gap * (group.rowSpan - 1)

    val currentOnLongPress by rememberUpdatedState(onLongPress)

    // Group header tile (only the header — expanded content is rendered separately)
    Box(
        modifier = modifier
            .width(tileWidth)
            .height(tileHeight)
            .clip(RoundedCornerShape(2.dp))
            .background(accentColor.copy(alpha = tileOpacity))
            .tileBevel(bevelEnabled, bevelDepth)
            .then(
                if (isBeingDragged) Modifier.border(
                    2.dp, Color.White, RoundedCornerShape(2.dp)
                ) else Modifier
            )
            .pointerInput(isEditMode) {
                detectTapGestures(
                    onTap = { if (isEditMode) onGroupEditTap() else onToggleExpand() },
                    onLongPress = if (isEditMode) null else { { currentOnLongPress() } }
                )
            }
    ) {
        // 2×2 grid of up to 4 app icons — no overlap
        val previewTiles = group.tiles.take(4)
        val iconSize = if (group.columnSpan <= 1) 16.dp else 24.dp
        val iconGap = 4.dp

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(iconGap),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-8).dp)
        ) {
            for (rowIdx in 0..1) {
                Row(horizontalArrangement = Arrangement.spacedBy(iconGap)) {
                    for (colIdx in 0..1) {
                        val tileIdx = rowIdx * 2 + colIdx
                        if (tileIdx < previewTiles.size) {
                            val tile = previewTiles[tileIdx]
                            val icon = iconMap[tile.packageName]
                            if (icon != null) {
                                val bitmap = remember(icon) {
                                    icon.toBitmap(96, 96).asImageBitmap()
                                }
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = tile.appName,
                                    modifier = Modifier.size(iconSize)
                                )
                            } else {
                                Spacer(Modifier.size(iconSize))
                            }
                        } else {
                            Spacer(Modifier.size(iconSize))
                        }
                    }
                }
            }
        }

        // Group name + count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 4.dp, end = 6.dp)
        ) {
            Text(
                text = "${group.name} (${group.tiles.size})",
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                              else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(14.dp)
            )
        }

        if (isEditMode) {
            Text(
                text = "${group.tiles.size} apps",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
        }
    }
}

/**
 * Expanded group content — rendered BETWEEN rows in StartScreen so it
 * doesn't push sibling tiles in the same row.
 *
 * Tiles are placed on a 6-column grid at their (groupCol, groupRow) positions,
 * allowing gaps between tiles. Tiles with groupCol/groupRow == -1 are auto-placed.
 */
@Composable
fun GroupExpandedContent(
    group: TileGroup,
    iconMap: Map<String, Drawable?>,
    accentColor: Color,
    tileOpacity: Float,
    bevelEnabled: Boolean,
    bevelDepth: Float,
    columnWidth: Dp,
    gap: Dp,
    isEditMode: Boolean,
    onTileTap: (String) -> Unit,
    onTileEditTap: (String) -> Unit,
    onSwapGroupTiles: (String, String) -> Unit,
    onMoveGroupTile: (String, Int, Int) -> Unit,
    onUngroupTile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridColumns = 6
    val expandedWidth = columnWidth * gridColumns + gap * (gridColumns - 1)

    // Intra-group drag state
    var dragChildId by remember { mutableStateOf<String?>(null) }
    var dragChildOffset by remember { mutableStateOf(Offset.Zero) }

    // Assign grid positions: use stored positions or auto-place
    data class PlacedTile(val tile: Tile, val col: Int, val row: Int)

    val placedTiles = remember(group.tiles) {
        placeTilesOnGrid(group.tiles, gridColumns)
    }

    val totalGridRows = if (placedTiles.isEmpty()) 0 else
        placedTiles.maxOf { it.row + it.tile.rowSpan }

    // Default minimum 2 rows high (6 wide is handled by expandedWidth);
    // expand beyond 2 only when tiles actually need more space.
    val displayGridRows = totalGridRows.coerceAtLeast(2)

    // Convert Dp values for pixel calculations
    val density = LocalDensity.current
    val columnWidthPx = with(density) { columnWidth.toPx() }
    val gapPx = with(density) { gap.toPx() }

    AnimatedVisibility(
        visible = true,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Column(modifier = Modifier.width(expandedWidth)) {
            // Top line + padding
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.4f))
            )
            Spacer(Modifier.height(8.dp))

            // Grid-based layout using Box with absolute positioning
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(
                        columnWidth * displayGridRows + gap * (displayGridRows - 1).coerceAtLeast(0)
                    )
            ) {
                for (placed in placedTiles) {
                    val tile = placed.tile
                    val isDraggingChild = dragChildId == tile.id
                    val childW = columnWidth * tile.columnSpan + gap * (tile.columnSpan - 1)
                    val childH = columnWidth * tile.rowSpan + gap * (tile.rowSpan - 1)

                    // Calculate pixel offset from grid position
                    val offsetX = (columnWidth + gap) * placed.col
                    val offsetY = (columnWidth + gap) * placed.row

                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .zIndex(if (isDraggingChild) 10f else 0f)
                            .graphicsLayer {
                                if (isDraggingChild) {
                                    translationX = dragChildOffset.x
                                    translationY = dragChildOffset.y
                                    shadowElevation = 16f
                                }
                            }
                    ) {
                        GroupChildTile(
                            tile = tile,
                            appIcon = iconMap[tile.packageName],
                            accentColor = accentColor,
                            tileOpacity = tileOpacity,
                            bevelEnabled = bevelEnabled,
                            bevelDepth = bevelDepth,
                            width = childW,
                            height = childH,
                            isEditMode = isEditMode,
                            isDragging = isDraggingChild
                        )

                        // Transparent overlay captures all gestures
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(tile.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            dragChildId = tile.id
                                            dragChildOffset = Offset.Zero
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragChildOffset = Offset(
                                                dragChildOffset.x + dragAmount.x,
                                                dragChildOffset.y + dragAmount.y
                                            )
                                        },
                                        onDragEnd = {
                                            // Calculate the target grid cell — round to nearest cell
                                            val cellW = columnWidthPx + gapPx
                                            val cellH = columnWidthPx + gapPx
                                            val originX = placed.col * cellW
                                            val originY = placed.row * cellH
                                            val dropX = originX + dragChildOffset.x
                                            val dropY = originY + dragChildOffset.y

                                            // Total expanded area bounds (in pixels)
                                            val totalW = gridColumns * cellW - gapPx
                                            val totalH = displayGridRows * cellH - gapPx

                                            // If dragged outside the expanded group area → ungroup
                                            if (dropX < -cellW / 2 || dropX > totalW + cellW / 2 ||
                                                dropY < -cellH / 2 || dropY > totalH + cellH / 2
                                            ) {
                                                onUngroupTile(tile.id)
                                            } else {
                                                val targetCol = kotlin.math.round(dropX / cellW).toInt()
                                                    .coerceIn(0, gridColumns - tile.columnSpan)
                                                val targetRow = kotlin.math.round(dropY / cellH).toInt()
                                                    .coerceAtLeast(0)
                                                onMoveGroupTile(tile.id, targetCol, targetRow)
                                            }

                                            dragChildId = null
                                            dragChildOffset = Offset.Zero
                                        },
                                        onDragCancel = {
                                            dragChildId = null
                                            dragChildOffset = Offset.Zero
                                        }
                                    )
                                }
                                .pointerInput(tile.id, isEditMode) {
                                    detectTapGestures(
                                        onTap = {
                                            if (isEditMode) {
                                                onTileEditTap(tile.id)
                                            } else {
                                                onTileTap(tile.packageName)
                                            }
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            // Bottom line + padding
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.4f))
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/**
 * Reusable bevel modifier — draws glass-like gradient edges on a composable.
 */
fun Modifier.tileBevel(enabled: Boolean, depth: Float): Modifier {
    if (!enabled || depth <= 0f) return this
    return this.drawWithContent {
        drawContent()
        val bevelWidth = (2.dp.toPx()) * depth
        // Top highlight
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.25f * depth.coerceAtMost(1f)), Color.Transparent),
                startY = 0f, endY = bevelWidth * 3
            ),
            size = size.copy(height = bevelWidth * 3)
        )
        // Left highlight
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White.copy(alpha = 0.18f * depth.coerceAtMost(1f)), Color.Transparent),
                startX = 0f, endX = bevelWidth * 3
            ),
            size = size.copy(width = bevelWidth * 3)
        )
        // Bottom shadow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.20f * depth.coerceAtMost(1f))),
                startY = size.height - bevelWidth * 3, endY = size.height
            ),
            topLeft = Offset(0f, size.height - bevelWidth * 3),
            size = size.copy(height = bevelWidth * 3)
        )
        // Right shadow
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f * depth.coerceAtMost(1f))),
                startX = size.width - bevelWidth * 3, endX = size.width
            ),
            topLeft = Offset(size.width - bevelWidth * 3, 0f),
            size = size.copy(width = bevelWidth * 3)
        )
    }
}

/**
 * A simplified tile rendering for group children — no internal gesture detectors.
 */
@Composable
private fun GroupChildTile(
    tile: Tile,
    appIcon: Drawable?,
    accentColor: Color,
    tileOpacity: Float,
    bevelEnabled: Boolean,
    bevelDepth: Float,
    width: Dp,
    height: Dp,
    isEditMode: Boolean,
    isDragging: Boolean
) {
    val tileColor = if (tile.colorOverride != null) Color(tile.colorOverride) else accentColor
    val opacity = tile.transparencyOverride ?: tileOpacity
    val isSmall = tile.columnSpan <= 1 && tile.rowSpan <= 1

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .scale(if (isDragging) 0.96f else if (isEditMode) 0.92f else 1f)
            .then(
                if (isDragging) Modifier.border(
                    2.dp, Color.White, RoundedCornerShape(2.dp)
                ) else Modifier
            )
            .clip(RoundedCornerShape(2.dp))
            .background(tileColor.copy(alpha = opacity))
            .tileBevel(bevelEnabled, bevelDepth)
    ) {
        if (appIcon != null) {
            val iconSize = if (isSmall) 36.dp else 60.dp
            val bitmap = remember(appIcon) {
                appIcon.toBitmap(128, 128).asImageBitmap()
            }
            Image(
                bitmap = bitmap,
                contentDescription = tile.appName,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.Center)
                    .then(if (!isSmall) Modifier.offset(y = (-8).dp) else Modifier)
            )
        }

        if (!isSmall) {
            Text(
                text = tile.appName,
                color = Color.White,
                fontSize = if (tile.columnSpan >= 4) 13.sp else 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 4.dp, end = 6.dp)
            )
        }

        if (isEditMode) {
            Text(
                text = "${tile.columnSpan}×${tile.rowSpan}",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
        }
    }
}

/**
 * Place tiles onto a grid, using stored (groupCol, groupRow) or auto-placing
 * tiles that have groupCol == -1.
 */
private data class PlacedTile(val tile: Tile, val col: Int, val row: Int)

private fun placeTilesOnGrid(tiles: List<Tile>, gridColumns: Int): List<PlacedTile> {
    val placed = mutableListOf<PlacedTile>()

    // First, place tiles that have explicit positions
    val explicit = tiles.filter { it.groupCol >= 0 && it.groupRow >= 0 }
    val needsAutoPlace = tiles.filter { it.groupCol < 0 || it.groupRow < 0 }

    for (tile in explicit) {
        val col = tile.groupCol.coerceIn(0, (gridColumns - tile.columnSpan).coerceAtLeast(0))
        val row = tile.groupRow.coerceAtLeast(0)
        placed.add(PlacedTile(tile, col, row))
    }

    // Build an occupancy grid from explicitly placed tiles
    fun isOccupied(col: Int, row: Int): Boolean {
        return placed.any { p ->
            col < p.col + p.tile.columnSpan &&
            col + 1 > p.col &&
            row < p.row + p.tile.rowSpan &&
            row + 1 > p.row
        }
    }

    fun canPlace(col: Int, row: Int, colSpan: Int, rowSpan: Int): Boolean {
        if (col + colSpan > gridColumns) return false
        for (c in col until col + colSpan) {
            for (r in row until row + rowSpan) {
                if (isOccupied(c, r)) return false
            }
        }
        return true
    }

    // Auto-place remaining tiles in first-fit fashion
    for (tile in needsAutoPlace) {
        val span = tile.columnSpan.coerceAtMost(gridColumns)
        var foundCol = 0
        var foundRow = 0
        var found = false

        for (r in 0..100) { // max 100 rows should be more than enough
            for (c in 0..(gridColumns - span)) {
                if (canPlace(c, r, span, tile.rowSpan)) {
                    foundCol = c
                    foundRow = r
                    found = true
                    break
                }
            }
            if (found) break
        }

        placed.add(PlacedTile(tile, foundCol, foundRow))
    }

    return placed
}
