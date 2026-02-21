package com.mylauncher.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mylauncher.data.model.AppInfo
import com.mylauncher.data.model.GridItem
import com.mylauncher.data.model.Tile
import com.mylauncher.data.model.TileGroup
import com.mylauncher.data.model.buildGridItems
import com.mylauncher.ui.components.GroupExpandedContent
import com.mylauncher.ui.components.GroupRenameDialog
import com.mylauncher.ui.components.GroupTileItem
import com.mylauncher.ui.components.TileItem
import com.mylauncher.ui.components.TileSettingsDialog
import kotlin.math.roundToInt

/**
 * The main Start Screen — a vertically scrolling Metro-style tile grid.
 * Long-press any tile to start dragging (auto-enters edit mode).
 * Drop on another tile to create a group; drop on empty space to reposition.
 * Tiles are placed at explicit grid coordinates — gaps are allowed.
 */
@Composable
fun StartScreen(
    tiles: List<Tile>,
    apps: List<AppInfo>,
    accentColor: Color,
    tileOpacity: Float,
    bevelEnabled: Boolean,
    bevelDepth: Float,
    isEditMode: Boolean,
    expandedGroups: Set<String>,
    onTileTap: (String) -> Unit,
    onTileLongPress: () -> Unit,
    onUnpinTile: (String) -> Unit,
    onSetTileSpans: (String, Int, Int) -> Unit,
    onToggleLiveTile: (String) -> Unit,
    onSwapTiles: (String, String) -> Unit,
    onCreateGroup: (String, String) -> Unit,
    onAddToGroup: (String, String) -> Unit,
    onUngroupTile: (String) -> Unit,
    onSwapGroupTiles: (String, String) -> Unit,
    onMoveGroupTile: (String, Int, Int) -> Unit,
    onToggleGroupExpanded: (String) -> Unit,
    onMoveTileToGrid: (String, Int, Int) -> Unit,
    onMoveGroupToGrid: (String, Int, Int) -> Unit,
    onRenameGroup: (String, String) -> Unit,
    onNavigateToAppList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onExitEditMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val horizontalPadding = 12.dp
    val gap = 4.dp
    val gridColumns = 6
    val availableWidth = screenWidthDp - (horizontalPadding * 2)
    val columnWidth = (availableWidth - gap * (gridColumns - 1)) / gridColumns

    // Grid cell step (width/height of one cell + gap)
    val rowHeight = columnWidth
    val cellStepX = columnWidth + gap
    val cellStepY = rowHeight + gap

    // Pixel equivalents for drag offset → grid cell conversion
    val density = LocalDensity.current
    val cellStepXPx = with(density) { cellStepX.toPx() }
    val cellStepYPx = with(density) { cellStepY.toPx() }

    // Local edit-mode interaction state
    var showSettingsForTileId by remember { mutableStateOf<String?>(null) }
    var showRenameForGroupId by remember { mutableStateOf<String?>(null) }

    // Drag state
    var dragItemKey by remember { mutableStateOf<String?>(null) }
    var dragIsGroup by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    // Settled-position dwell tracking for grouping: finger must stay within a
    // small radius of its "settled" position for groupDwellMs to create a group.
    // Natural hand tremor (<20px) is tolerated; any larger movement resets.
    var hoverTargetKey by remember { mutableStateOf<String?>(null) }
    var hoverSettledAt by remember { mutableStateOf(Offset.Zero) }
    var hoverSettledTime by remember { mutableStateOf(0L) }
    val groupDwellMs = 800L   // must hold still this long to trigger grouping
    val settledRadiusPx = 50f // ~19dp — movement within this radius is "still"

    // Drop target outline: tracks where the dragged item will land
    data class DropTarget(val col: Int, val row: Int, val colSpan: Int, val rowSpan: Int)
    var dropTarget by remember { mutableStateOf<DropTarget?>(null) }

    // Clear local state when exiting edit mode
    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            showSettingsForTileId = null
            dragItemKey = null
            dragOffset = Offset.Zero
            hoverTargetKey = null
            hoverSettledAt = Offset.Zero
            hoverSettledTime = 0L
            dropTarget = null
        }
    }

    // Build icon lookup map
    val iconMap: Map<String, Drawable?> = remember(apps) {
        apps.associate { it.packageName to it.icon }
    }

    // Build grid items (Singles + Groups) with grid coordinates
    val gridItems = remember(tiles) { buildGridItems(tiles) }

    // Keep updated refs for use inside gesture callbacks without resetting pointerInput
    val currentGridItems by rememberUpdatedState(gridItems)
    val currentIsEditMode by rememberUpdatedState(isEditMode)

    // Determine how many visual grid rows exist (add extra rows for drag targets)
    val maxGridRow = remember(gridItems) {
        (gridItems.maxOfOrNull { it.gridRow + it.rowSpan } ?: 0)
    }
    val totalRows = maxGridRow + 4 // extra empty rows below content for drop targets

    // Pre-compute which rows should show expanded group content after them
    val expansionAfterRow = remember(gridItems, expandedGroups) {
        val map = mutableMapOf<Int, MutableList<GridItem.Group>>()
        for (item in gridItems) {
            if (item is GridItem.Group && item.group.groupId in expandedGroups) {
                val endRow = item.gridRow + item.rowSpan - 1
                map.getOrPut(endRow) { mutableListOf() }.add(item)
            }
        }
        map
    }

    /**
     * Common drag-gesture modifier for tiles and groups.
     * Long press => enter edit mode + start drag.
     * Drop logic:
     *   - If hovering over another tile for 500ms+ → create group / add to group
     *   - Otherwise → move tile/group to that cell (displacing neighbors)
     */
    fun Modifier.tileDragGesture(itemKey: String, isGroup: Boolean): Modifier =
        this.pointerInput(itemKey) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    if (!currentIsEditMode) onTileLongPress()
                    dragItemKey = itemKey
                    dragIsGroup = isGroup
                    dragOffset = Offset.Zero
                    hoverTargetKey = null
                    hoverSettledAt = Offset.Zero
                    hoverSettledTime = 0L
                    dropTarget = null
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    dragOffset = Offset(
                        dragOffset.x + dragAmount.x,
                        dragOffset.y + dragAmount.y
                    )

                    // Settled-position radius check: if finger has drifted more than
                    // settledRadiusPx from the last "settled" position, reset.
                    val driftDist = kotlin.math.sqrt(
                        (dragOffset.x - hoverSettledAt.x).let { it * it } +
                        (dragOffset.y - hoverSettledAt.y).let { it * it }
                    )
                    if (driftDist > settledRadiusPx) {
                        hoverSettledAt = dragOffset
                        hoverSettledTime = System.currentTimeMillis()
                    }

                    // Compute and publish the drop-target position for the outline
                    val items = currentGridItems
                    val draggedItem = items.find { item ->
                        when (item) {
                            is GridItem.Single -> item.tile.id == itemKey
                            is GridItem.Group -> item.group.groupId == itemKey
                        }
                    }
                    if (draggedItem != null) {
                        val colOff = (dragOffset.x / cellStepXPx).roundToInt()
                        val rowOff = (dragOffset.y / cellStepYPx).roundToInt()
                        val tCol = (draggedItem.gridCol + colOff).coerceIn(0, gridColumns - draggedItem.colSpan)
                        val tRow = (draggedItem.gridRow + rowOff).coerceAtLeast(0)

                        // Update the visible drop target outline
                        dropTarget = DropTarget(tCol, tRow, draggedItem.colSpan, draggedItem.rowSpan)

                        val cCol = tCol + draggedItem.colSpan / 2.0
                        val cRow = tRow + draggedItem.rowSpan / 2.0

                        val currentHover = items.find { item ->
                            val k = when (item) {
                                is GridItem.Single -> item.tile.id
                                is GridItem.Group -> item.group.groupId
                            }
                            if (k == itemKey) return@find false
                            cCol >= item.gridCol && cCol < item.gridCol + item.colSpan &&
                            cRow >= item.gridRow && cRow < item.gridRow + item.rowSpan
                        }
                        val currentHoverKey = when (currentHover) {
                            is GridItem.Single -> currentHover.tile.id
                            is GridItem.Group -> currentHover.group.groupId
                            null -> null
                        }
                        if (!isGroup) {
                            if (currentHoverKey != hoverTargetKey) {
                                hoverTargetKey = currentHoverKey
                                // Reset settled position when entering a new target
                                hoverSettledAt = dragOffset
                                hoverSettledTime = System.currentTimeMillis()
                            }
                        }
                    }
                },
                onDragEnd = {
                    val items = currentGridItems
                    val draggedItem = items.find { item ->
                        when (item) {
                            is GridItem.Single -> item.tile.id == itemKey
                            is GridItem.Group -> item.group.groupId == itemKey
                        }
                    }

                    if (draggedItem != null) {
                        val currentCol = draggedItem.gridCol
                        val currentRow = draggedItem.gridRow
                        val dragColSpan = draggedItem.colSpan
                        val dragRowSpan = draggedItem.rowSpan

                        val colOffset = (dragOffset.x / cellStepXPx).roundToInt()
                        val rowOffset = (dragOffset.y / cellStepYPx).roundToInt()

                        val targetCol = (currentCol + colOffset).coerceIn(0, gridColumns - dragColSpan)
                        val targetRow = (currentRow + rowOffset).coerceAtLeast(0)

                        // Only group if finger has stayed within settled radius for 800ms+
                        val dwellOk = hoverTargetKey != null &&
                                hoverSettledTime > 0L &&
                                (System.currentTimeMillis() - hoverSettledTime) >= groupDwellMs
                        val groupTarget = if (!isGroup && dwellOk) {
                            val centerCol = targetCol + dragColSpan / 2.0
                            val centerRow = targetRow + dragRowSpan / 2.0
                            items.find { item ->
                                val key = when (item) {
                                    is GridItem.Single -> item.tile.id
                                    is GridItem.Group -> item.group.groupId
                                }
                                if (key == itemKey) return@find false
                                centerCol >= item.gridCol && centerCol < item.gridCol + item.colSpan &&
                                centerRow >= item.gridRow && centerRow < item.gridRow + item.rowSpan
                            }
                        } else null

                        when {
                            groupTarget is GridItem.Single -> {
                                onCreateGroup(itemKey, groupTarget.tile.id)
                            }
                            groupTarget is GridItem.Group -> {
                                onAddToGroup(itemKey, groupTarget.group.groupId)
                            }
                            else -> {
                                if (isGroup) {
                                    onMoveGroupToGrid(itemKey, targetCol, targetRow)
                                } else {
                                    onMoveTileToGrid(itemKey, targetCol, targetRow)
                                }
                            }
                        }
                    }

                    dragItemKey = null
                    dragOffset = Offset.Zero
                    hoverTargetKey = null
                    hoverSettledAt = Offset.Zero
                    hoverSettledTime = 0L
                    dropTarget = null
                },
                onDragCancel = {
                    dragItemKey = null
                    dragOffset = Offset.Zero
                    hoverTargetKey = null
                    hoverSettledAt = Offset.Zero
                    hoverSettledTime = 0L
                    dropTarget = null
                }
            )
        }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = 48.dp,
                    bottom = 80.dp
                )
        ) {
            // Render each grid row. Items are positioned within each row's
            // Box by their gridCol offset. Multi-row tiles overflow downward.
            for (rowIdx in 0 until totalRows) {
                val rowItems = gridItems.filter { it.gridRow == rowIdx }

                if (rowItems.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(rowHeight)) {
                        // Drop target outline: render if this row is the target row
                        val dt = dropTarget
                        if (dt != null && dt.row == rowIdx) {
                            val outlineW = columnWidth * dt.colSpan + gap * (dt.colSpan - 1)
                            val outlineH = columnWidth * dt.rowSpan + gap * (dt.rowSpan - 1)
                            Box(
                                modifier = Modifier
                                    .offset(x = cellStepX * dt.col)
                                    .wrapContentHeight(align = Alignment.Top, unbounded = true)
                                    .width(outlineW)
                                    .height(outlineH)
                                    .zIndex(5f)
                                    .border(
                                        width = 2.dp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                        for (item in rowItems) {
                            val itemKey = when (item) {
                                is GridItem.Single -> item.tile.id
                                is GridItem.Group -> item.group.groupId
                            }
                            key(itemKey) {
                            val xOffset: Dp = cellStepX * item.gridCol

                            when (item) {
                                is GridItem.Single -> {
                                    val tile = item.tile
                                    val isDragging = dragItemKey == tile.id && !dragIsGroup
                                    Box(
                                        modifier = Modifier
                                            .offset(x = xOffset)
                                            .wrapContentHeight(align = Alignment.Top, unbounded = true)
                                            .zIndex(if (isDragging) 10f else 0f)
                                            .graphicsLayer {
                                                if (isDragging) {
                                                    translationX = dragOffset.x
                                                    translationY = dragOffset.y
                                                    shadowElevation = 16f
                                                }
                                            }
                                            .tileDragGesture(tile.id, isGroup = false)
                                    ) {
                                        TileItem(
                                            tile = tile,
                                            appIcon = iconMap[tile.packageName],
                                            accentColor = accentColor,
                                            tileOpacity = tileOpacity,
                                            bevelEnabled = bevelEnabled,
                                            bevelDepth = bevelDepth,
                                            columnWidth = columnWidth,
                                            gap = gap,
                                            isEditMode = isEditMode,
                                            isSelectedForMove = isDragging,
                                            onTileTap = {
                                                if (!isEditMode) {
                                                    onTileTap(tile.packageName)
                                                } else {
                                                    showSettingsForTileId = tile.id
                                                }
                                            },
                                            onTileLongPress = { /* handled by drag gesture */ },
                                            onUnpin = { onUnpinTile(tile.id) }
                                        )
                                    }
                                }
                                is GridItem.Group -> {
                                    val group = item.group
                                    val isDragging = dragItemKey == group.groupId && dragIsGroup
                                    Box(
                                        modifier = Modifier
                                            .offset(x = xOffset)
                                            .wrapContentHeight(align = Alignment.Top, unbounded = true)
                                            .zIndex(if (isDragging) 10f else 0f)
                                            .graphicsLayer {
                                                if (isDragging) {
                                                    translationX = dragOffset.x
                                                    translationY = dragOffset.y
                                                    shadowElevation = 16f
                                                }
                                            }
                                            .tileDragGesture(group.groupId, isGroup = true)
                                    ) {
                                        GroupTileItem(
                                            group = group,
                                            iconMap = iconMap,
                                            accentColor = accentColor,
                                            tileOpacity = tileOpacity,
                                            bevelEnabled = bevelEnabled,
                                            bevelDepth = bevelDepth,
                                            columnWidth = columnWidth,
                                            gap = gap,
                                            isEditMode = isEditMode,
                                            isExpanded = group.groupId in expandedGroups,
                                            isBeingDragged = isDragging,
                                            onToggleExpand = { onToggleGroupExpanded(group.groupId) },
                                            onTileTap = { packageName -> onTileTap(packageName) },
                                            onTileEditTap = { tileId -> showSettingsForTileId = tileId },
                                            onLongPress = { /* handled by drag */ },
                                            onUngroupTile = { tileId -> onUngroupTile(tileId) },
                                            onSwapGroupTiles = { id1, id2 -> onSwapGroupTiles(id1, id2) },
                                            onGroupEditTap = { showRenameForGroupId = group.groupId }
                                        )
                                    }
                                }
                            }
                            } // key
                        }
                    }
                } else {
                    // Empty grid row — render spacer (or drop target outline)
                    val dt = dropTarget
                    if (dt != null && dt.row == rowIdx) {
                        Box(modifier = Modifier.fillMaxWidth().height(rowHeight)) {
                            val outlineW = columnWidth * dt.colSpan + gap * (dt.colSpan - 1)
                            val outlineH = columnWidth * dt.rowSpan + gap * (dt.rowSpan - 1)
                            Box(
                                modifier = Modifier
                                    .offset(x = cellStepX * dt.col)
                                    .wrapContentHeight(align = Alignment.Top, unbounded = true)
                                    .width(outlineW)
                                    .height(outlineH)
                                    .zIndex(5f)
                                    .border(
                                        width = 2.dp,
                                        color = Color.White.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.fillMaxWidth().height(rowHeight))
                    }
                }

                // Gap between rows
                Spacer(modifier = Modifier.height(gap))

                // Render expanded group content below the last row of the group
                expansionAfterRow[rowIdx]?.forEach { groupItem ->
                    GroupExpandedContent(
                        group = groupItem.group,
                        iconMap = iconMap,
                        accentColor = accentColor,
                        tileOpacity = tileOpacity,
                        bevelEnabled = bevelEnabled,
                        bevelDepth = bevelDepth,
                        columnWidth = columnWidth,
                        gap = gap,
                        isEditMode = isEditMode,
                        onTileTap = { packageName -> onTileTap(packageName) },
                        onTileEditTap = { tileId -> showSettingsForTileId = tileId },
                        onSwapGroupTiles = { id1, id2 -> onSwapGroupTiles(id1, id2) },
                        onMoveGroupTile = { tileId, col, row -> onMoveGroupTile(tileId, col, row) },
                        onUngroupTile = { tileId -> onUngroupTile(tileId) }
                    )
                    Spacer(modifier = Modifier.height(gap))
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
                    containerColor = accentColor
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Done", color = Color.White)
            }
        }

        // Tile settings dialog
        val settingsTile = tiles.find { it.id == showSettingsForTileId }
        if (settingsTile != null) {
            TileSettingsDialog(
                tile = settingsTile,
                gridColumns = gridColumns,
                accentColor = accentColor,
                onDismiss = { showSettingsForTileId = null },
                onSetSpans = { colSpan, rowSpan -> onSetTileSpans(settingsTile.id, colSpan, rowSpan) },
                onToggleLiveTile = { onToggleLiveTile(settingsTile.id) }
            )
        }

        // Group rename dialog
        val renameGroupItem = gridItems.filterIsInstance<GridItem.Group>()
            .find { it.group.groupId == showRenameForGroupId }
        if (renameGroupItem != null) {
            GroupRenameDialog(
                currentName = renameGroupItem.group.name,
                accentColor = accentColor,
                onDismiss = { showRenameForGroupId = null },
                onRename = { newName ->
                    onRenameGroup(renameGroupItem.group.groupId, newName)
                    showRenameForGroupId = null
                }
            )
        }
    }
}
