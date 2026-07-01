package com.mylauncher.ui.screens

import android.graphics.drawable.Drawable
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.mylauncher.ui.components.WidgetTileItem
import com.mylauncher.service.LiveTileInfo
import com.mylauncher.widget.LauncherWidgetHost
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
    wallpaperOnlyInTiles: Boolean = false,
    isEditMode: Boolean,
    expandedGroups: Set<String>,
    onTileTap: (String, Long) -> Unit,
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
    onSetGroupSpans: (String, Int, Int) -> Unit,
    onNavigateToAppList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onExitEditMode: () -> Unit,
    onChangeWallpaper: () -> Unit,
    onAddWidget: () -> Unit,
    onUpdateGridColumns: (Int) -> Unit = {},
    liveTileData: Map<String, LiveTileInfo> = emptyMap(),
    widgetHost: LauncherWidgetHost? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val horizontalPadding = 12.dp
    val gap = 4.dp

    // Derive a consistent cell size from the shorter dimension (portrait width),
    // then compute how many columns fit in the current width so the grid fills
    // the screen while tiles keep the same physical size in any orientation.
    val portraitWidth = minOf(screenWidthDp, screenHeightDp)
    val referenceAvailable = portraitWidth - (horizontalPadding * 2)
    val referenceCellWidth = (referenceAvailable - gap * 5) / 6  // portrait: 6 columns
    val referenceCellStep = referenceCellWidth + gap

    val availableWidth = screenWidthDp - (horizontalPadding * 2)
    val gridColumns = ((availableWidth + gap) / referenceCellStep).toInt().coerceAtLeast(6)
    val columnWidth = (availableWidth - gap * (gridColumns - 1)) / gridColumns

    // Notify the repository when the active column count changes (orientation)
    LaunchedEffect(gridColumns) {
        onUpdateGridColumns(gridColumns)
    }

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
    var dragStartContentY by remember { mutableStateOf(0f) }  // finger Y in content coords at drag start
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
                onDragStart = { startOffset ->
                    if (!currentIsEditMode) onTileLongPress()
                    dragItemKey = itemKey
                    dragIsGroup = isGroup
                    dragOffset = Offset.Zero
                    hoverTargetKey = null
                    hoverSettledAt = Offset.Zero
                    hoverSettledTime = 0L
                    dropTarget = null

                    // Compute accurate finger content-Y for auto-scroll.
                    // startOffset is relative to the tile's own Box.
                    val items = currentGridItems
                    val draggedItem = items.find { item ->
                        when (item) {
                            is GridItem.Single -> item.tile.id == itemKey
                            is GridItem.Group -> item.group.groupId == itemKey
                        }
                    }
                    if (draggedItem != null) {
                        val topPadPx = with(density) { 48.dp.toPx() }
                        dragStartContentY = topPadPx + draggedItem.gridRow * cellStepYPx + startOffset.y
                    }
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

    // Scroll state — retained so auto-scroll during drag can manipulate it
    val scrollState = rememberScrollState()

    // Auto-scroll when the drag finger is near the top or bottom edge.
    val autoScrollThreshold = with(density) { 80.dp.toPx() }  // edge zone size
    val autoScrollMaxSpeed = with(density) { 12.dp.toPx() }   // max px per frame
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Continuous auto-scroll loop: runs while a drag is active.
    // Reads dragOffset + scrollState each frame for accurate finger Y.
    LaunchedEffect(dragItemKey) {
        if (dragItemKey == null) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(16L) // ~60 fps
            val fingerScreenY = dragStartContentY - scrollState.value + dragOffset.y

            when {
                fingerScreenY < autoScrollThreshold -> {
                    // Proportional speed: closer to edge = faster
                    val intensity = 1f - (fingerScreenY / autoScrollThreshold).coerceIn(0f, 1f)
                    val amount = -(autoScrollMaxSpeed * intensity).toInt().coerceAtLeast(1)
                    scrollState.scrollTo((scrollState.value + amount).coerceAtLeast(0))
                }
                fingerScreenY > screenHeightPx - autoScrollThreshold -> {
                    val intensity = 1f - ((screenHeightPx - fingerScreenY) / autoScrollThreshold).coerceIn(0f, 1f)
                    val amount = (autoScrollMaxSpeed * intensity).toInt().coerceAtLeast(1)
                    scrollState.scrollTo((scrollState.value + amount).coerceAtMost(scrollState.maxValue))
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {

        // When "wallpaper only in tiles" is enabled, draw a black mask over
        // the entire screen and punch transparent holes at each tile's
        // position so the system wallpaper shows through only there.
        if (wallpaperOnlyInTiles) {
            val topPaddingPx = with(density) { 48.dp.toPx() }
            val hPadPx = with(density) { horizontalPadding.toPx() }
            val gapPx = with(density) { gap.toPx() }
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }
            ) {
                drawRect(Color.Black)
                val scrollPx = scrollState.value.toFloat()
                for (item in gridItems) {
                    val x = hPadPx + item.gridCol * cellStepXPx
                    val y = topPaddingPx + item.gridRow * cellStepYPx - scrollPx
                    val w = item.colSpan * cellStepXPx - gapPx
                    val h = item.rowSpan * cellStepYPx - gapPx
                    drawRect(
                        color = Color.Transparent,
                        topLeft = androidx.compose.ui.geometry.Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(w, h),
                        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            if (!currentIsEditMode) onTileLongPress()
                        }
                    )
                }
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
                                        if (tile.isWidget && widgetHost != null) {
                                            WidgetTileItem(
                                                tile = tile,
                                                widgetHost = widgetHost,
                                                columnWidth = columnWidth,
                                                gap = gap,
                                                isEditMode = isEditMode,
                                                onEditTap = { showSettingsForTileId = tile.id },
                                                onUnpin = { onUnpinTile(tile.id) }
                                            )
                                        } else {
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
                                                        onTileTap(tile.packageName, tile.userSerialNumber)
                                                    } else {
                                                        showSettingsForTileId = tile.id
                                                    }
                                                },
                                                onTileLongPress = { /* handled by drag gesture */ },
                                                onUnpin = { onUnpinTile(tile.id) },
                                                liveTileInfo = if (tile.isLiveTile) liveTileData[tile.packageName] else null
                                            )
                                        }
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
                                            onTileTap = { packageName, serial -> onTileTap(packageName, serial) },
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
                        onTileTap = { packageName, serial -> onTileTap(packageName, serial) },
                        onTileEditTap = { tileId -> showSettingsForTileId = tileId },
                        onSwapGroupTiles = { id1, id2 -> onSwapGroupTiles(id1, id2) },
                        onMoveGroupTile = { tileId, col, row -> onMoveGroupTile(tileId, col, row) },
                        onUngroupTile = { tileId -> onUngroupTile(tileId) },
                        gridColumns = gridColumns
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
                    .navigationBarsPadding()
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
                modifier = Modifier.navigationBarsPadding().padding(start = 8.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Edit-mode bottom bar: Wallpaper | Widgets | Settings | Done
        AnimatedVisibility(
            visible = isEditMode,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 10.dp)
                ) {
                    // Wallpaper
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onChangeWallpaper() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = "Wallpaper",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Wallpaper", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    // Widgets
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onAddWidget() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Widgets,
                            contentDescription = "Widgets",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Widgets", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    // Preferences
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onNavigateToSettings() }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Preferences",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text("Preferences", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    // Done
                    Button(
                        onClick = onExitEditMode,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text("Done", color = Color.White)
                    }
                }
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

        // Group settings dialog (rename + resize)
        val renameGroupItem = gridItems.filterIsInstance<GridItem.Group>()
            .find { it.group.groupId == showRenameForGroupId }
        if (renameGroupItem != null) {
            GroupRenameDialog(
                currentName = renameGroupItem.group.name,
                currentColSpan = renameGroupItem.group.columnSpan,
                currentRowSpan = renameGroupItem.group.rowSpan,
                gridColumns = gridColumns,
                accentColor = accentColor,
                onDismiss = { showRenameForGroupId = null },
                onRename = { newName ->
                    onRenameGroup(renameGroupItem.group.groupId, newName)
                },
                onSetSpans = { colSpan, rowSpan ->
                    onSetGroupSpans(renameGroupItem.group.groupId, colSpan, rowSpan)
                }
            )
        }
    }
}
