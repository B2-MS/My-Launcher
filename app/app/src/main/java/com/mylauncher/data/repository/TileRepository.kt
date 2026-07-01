package com.mylauncher.data.repository

import com.mylauncher.data.model.Tile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory tile repository for the prototype.
 * TODO: Replace with Room persistence for production.
 */
@Singleton
class TileRepository @Inject constructor() {

    private val _tiles = MutableStateFlow<List<Tile>>(emptyList())
    val tiles: StateFlow<List<Tile>> = _tiles.asStateFlow()

    /** Tracks which groups are currently expanded in the UI. */
    private val _expandedGroups = MutableStateFlow<Set<String>>(emptySet())
    val expandedGroups: StateFlow<Set<String>> = _expandedGroups.asStateFlow()

    var gridColumns = 6
        private set

    /**
     * Update the grid column count (e.g. when orientation changes).
     * Reflows all tiles into the new column width while preserving their sizes
     * and relative ordering.
     */
    fun setGridColumns(columns: Int) {
        if (columns == gridColumns) return
        gridColumns = columns.coerceAtLeast(6)

        val current = _tiles.value
        if (current.isEmpty()) return

        // Collect grid entries: standalone tiles and groups (deduplicated)
        data class GridEntry(
            val key: String,        // tileId or groupId
            val isGroup: Boolean,
            val col: Int,
            val row: Int,
            val colSpan: Int,
            val rowSpan: Int
        )

        val entries = mutableListOf<GridEntry>()
        val seenGroups = mutableSetOf<String>()

        for (tile in current) {
            if (tile.gridCol < 0 || tile.gridRow < 0) continue
            val gid = tile.groupId
            if (gid != null) {
                if (gid in seenGroups) continue
                seenGroups.add(gid)
                entries.add(GridEntry(gid, true, tile.gridCol, tile.gridRow, 2, 2))
            } else {
                entries.add(GridEntry(tile.id, false, tile.gridCol, tile.gridRow, tile.columnSpan, tile.rowSpan))
            }
        }

        // Sort by original position: top-to-bottom, left-to-right
        entries.sortWith(compareBy({ it.row }, { it.col }))

        // Re-place each entry into the new grid using first-fit
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val newPositions = mutableMapOf<String, Pair<Int, Int>>()

        for (entry in entries) {
            // Clamp span to grid width if needed
            val cSpan = entry.colSpan.coerceAtMost(gridColumns)
            val rSpan = entry.rowSpan
            val (col, row) = findNextAvailableCellFromSet(occupied, cSpan, rSpan)
            for (dc in 0 until cSpan) {
                for (dr in 0 until rSpan) {
                    occupied.add(col + dc to row + dr)
                }
            }
            newPositions[entry.key] = col to row
        }

        // Apply new positions to all tiles
        _tiles.value = current.map { tile ->
            val gid = tile.groupId
            if (gid != null) {
                val pos = newPositions[gid]
                if (pos != null) tile.copy(gridCol = pos.first, gridRow = pos.second) else tile
            } else {
                val pos = newPositions[tile.id]
                if (pos != null) {
                    val cSpan = tile.columnSpan.coerceAtMost(gridColumns)
                    if (cSpan != tile.columnSpan) {
                        tile.copy(gridCol = pos.first, gridRow = pos.second, columnSpan = cSpan)
                    } else {
                        tile.copy(gridCol = pos.first, gridRow = pos.second)
                    }
                } else tile
            }
        }
    }

    fun setTiles(tiles: List<Tile>) {
        _tiles.value = autoAssignGridPositions(tiles).sortedBy { it.position }
    }

    fun addTile(tile: Tile) {
        val current = _tiles.value.toMutableList()
        val newPosition = (current.maxOfOrNull { it.position } ?: -1) + 1
        val placed = if (tile.gridCol < 0 || tile.gridRow < 0) {
            val (col, row) = findNextAvailableCell(current, tile.columnSpan, tile.rowSpan)
            tile.copy(position = newPosition, gridCol = col, gridRow = row)
        } else {
            tile.copy(position = newPosition)
        }
        current.add(placed)
        _tiles.value = current
    }

    fun removeTile(tileId: String) {
        _tiles.value = _tiles.value.filter { it.id != tileId }
    }

    fun updateTile(tile: Tile) {
        _tiles.value = _tiles.value.map { if (it.id == tile.id) tile else it }
    }

    fun setTileSpans(tileId: String, columnSpan: Int, rowSpan: Int) {
        val clampedCol = columnSpan.coerceIn(1, 6)
        // 4+ columns wide → minimum 2 rows high
        val minRow = if (clampedCol >= 4) 2 else 1
        val clampedRow = rowSpan.coerceIn(minRow, 4)

        val current = _tiles.value
        val target = current.find { it.id == tileId } ?: return

        // Apply the new size
        val resized = target.copy(columnSpan = clampedCol, rowSpan = clampedRow)

        // Build the set of cells the resized tile will occupy
        val resizedCells = mutableSetOf<Pair<Int, Int>>()
        if (resized.gridCol >= 0 && resized.gridRow >= 0) {
            for (c in resized.gridCol until resized.gridCol + clampedCol) {
                for (r in resized.gridRow until resized.gridRow + clampedRow) {
                    resizedCells.add(c to r)
                }
            }
        }

        // Identify tiles/groups that now overlap with the resized tile
        val seenGroups = mutableSetOf<String>()
        val displacedIds = mutableSetOf<String>() // tile IDs or group representative IDs
        val displacedGroupIds = mutableSetOf<String>()

        for (tile in current) {
            if (tile.id == tileId) continue
            if (tile.gridCol < 0 || tile.gridRow < 0) continue

            val gid = tile.groupId
            if (gid != null) {
                if (gid in seenGroups) continue
                seenGroups.add(gid)
                // Groups occupy 2×2
                val overlaps = (tile.gridCol until tile.gridCol + 2).any { c ->
                    (tile.gridRow until tile.gridRow + 2).any { r ->
                        (c to r) in resizedCells
                    }
                }
                if (overlaps) displacedGroupIds.add(gid)
            } else {
                val overlaps = (tile.gridCol until tile.gridCol + tile.columnSpan).any { c ->
                    (tile.gridRow until tile.gridRow + tile.rowSpan).any { r ->
                        (c to r) in resizedCells
                    }
                }
                if (overlaps) displacedIds.add(tile.id)
            }
        }

        // Start with the resized tile in place, build an occupancy set
        var updated = current.map { if (it.id == tileId) resized else it }
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val processedGroups = mutableSetOf<String>()

        // Mark cells for all non-displaced items
        for (tile in updated) {
            if (tile.gridCol < 0 || tile.gridRow < 0) continue
            if (tile.id in displacedIds) continue
            val gid = tile.groupId
            if (gid != null && gid in displacedGroupIds) continue

            if (gid != null) {
                if (gid in processedGroups) continue
                processedGroups.add(gid)
                for (c in tile.gridCol until tile.gridCol + 2) {
                    for (r in tile.gridRow until tile.gridRow + 2) {
                        occupied.add(c to r)
                    }
                }
            } else {
                for (c in tile.gridCol until tile.gridCol + tile.columnSpan) {
                    for (r in tile.gridRow until tile.gridRow + tile.rowSpan) {
                        occupied.add(c to r)
                    }
                }
            }
        }

        // Relocate displaced standalone tiles
        for (dId in displacedIds) {
            val tile = updated.find { it.id == dId } ?: continue
            val (newCol, newRow) = findNextAvailableCellFromSet(occupied, tile.columnSpan, tile.rowSpan)
            updated = updated.map { if (it.id == dId) it.copy(gridCol = newCol, gridRow = newRow) else it }
            for (c in newCol until newCol + tile.columnSpan) {
                for (r in newRow until newRow + tile.rowSpan) {
                    occupied.add(c to r)
                }
            }
        }

        // Relocate displaced groups
        for (gId in displacedGroupIds) {
            val (newCol, newRow) = findNextAvailableCellFromSet(occupied, 2, 2)
            updated = updated.map { if (it.groupId == gId) it.copy(gridCol = newCol, gridRow = newRow) else it }
            for (c in newCol until newCol + 2) {
                for (r in newRow until newRow + 2) {
                    occupied.add(c to r)
                }
            }
        }

        _tiles.value = updated
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        val current = _tiles.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _tiles.value = current.mapIndexed { index, tile -> tile.copy(position = index) }
        }
    }

    fun toggleLiveTile(tileId: String) {
        _tiles.value = _tiles.value.map { tile ->
            if (tile.id == tileId) tile.copy(isLiveTile = !tile.isLiveTile) else tile
        }
    }

    fun swapTiles(tileId1: String, tileId2: String) {
        val current = _tiles.value
        val index1 = current.indexOfFirst { it.id == tileId1 }
        val index2 = current.indexOfFirst { it.id == tileId2 }
        if (index1 >= 0 && index2 >= 0) {
            val mutable = current.toMutableList()
            val tile1 = mutable[index1]
            val tile2 = mutable[index2]
            mutable[index1] = tile2.copy(position = tile1.position, gridCol = tile1.gridCol, gridRow = tile1.gridRow)
            mutable[index2] = tile1.copy(position = tile2.position, gridCol = tile2.gridCol, gridRow = tile2.gridRow)
            _tiles.value = mutable.sortedBy { it.position }
        }
    }

    // ─────────────── Grid movement operations ───────────────

    /** Move a single tile to specific grid coordinates, displacing any overlapping items. */
    fun moveTileToGrid(tileId: String, col: Int, row: Int) {
        val current = _tiles.value
        val tile = current.find { it.id == tileId } ?: return
        val cCol = col.coerceIn(0, (gridColumns - tile.columnSpan).coerceAtLeast(0))
        val cRow = row.coerceAtLeast(0)

        // Move tile first
        var updated = current.map {
            if (it.id == tileId) it.copy(gridCol = cCol, gridRow = cRow) else it
        }
        _tiles.value = displaceOverlappingItems(updated, excludeId = tileId)
    }

    /** Move all tiles in a group to specific grid coordinates, displacing any overlapping items. */
    fun moveGroupToGrid(groupId: String, col: Int, row: Int) {
        val current = _tiles.value
        // Groups are always 2×2 on the main grid
        val cCol = col.coerceIn(0, (gridColumns - 2).coerceAtLeast(0))
        val cRow = row.coerceAtLeast(0)

        var updated = current.map { tile ->
            if (tile.groupId == groupId) tile.copy(gridCol = cCol, gridRow = cRow) else tile
        }
        _tiles.value = displaceOverlappingItems(updated, excludeGroupId = groupId)
    }

    /**
     * After placing a tile/group at a new position, find any other items that
     * now overlap and relocate them to the nearest available cell.
     */
    private fun displaceOverlappingItems(
        tiles: List<Tile>,
        excludeId: String? = null,
        excludeGroupId: String? = null
    ): List<Tile> {
        // Build the cells occupied by the moved item
        val movedCells = mutableSetOf<Pair<Int, Int>>()
        val seenMovedGroups = mutableSetOf<String>()
        for (tile in tiles) {
            if (excludeId != null && tile.id == excludeId) {
                for (c in tile.gridCol until tile.gridCol + tile.columnSpan) {
                    for (r in tile.gridRow until tile.gridRow + tile.rowSpan) {
                        movedCells.add(c to r)
                    }
                }
            } else if (excludeGroupId != null && tile.groupId == excludeGroupId) {
                if (tile.groupId!! !in seenMovedGroups) {
                    seenMovedGroups.add(tile.groupId!!)
                    for (c in tile.gridCol until tile.gridCol + 2) {
                        for (r in tile.gridRow until tile.gridRow + 2) {
                            movedCells.add(c to r)
                        }
                    }
                }
            }
        }

        // Find items that overlap the moved item
        val displacedTileIds = mutableSetOf<String>()
        val displacedGroupIds = mutableSetOf<String>()
        val seenGroups = mutableSetOf<String>()

        for (tile in tiles) {
            if (tile.id == excludeId) continue
            if (tile.groupId == excludeGroupId && excludeGroupId != null) continue
            if (tile.gridCol < 0 || tile.gridRow < 0) continue

            val gid = tile.groupId
            if (gid != null) {
                if (gid in seenGroups) continue
                seenGroups.add(gid)
                val overlaps = (tile.gridCol until tile.gridCol + 2).any { c ->
                    (tile.gridRow until tile.gridRow + 2).any { r -> (c to r) in movedCells }
                }
                if (overlaps) displacedGroupIds.add(gid)
            } else {
                val overlaps = (tile.gridCol until tile.gridCol + tile.columnSpan).any { c ->
                    (tile.gridRow until tile.gridRow + tile.rowSpan).any { r -> (c to r) in movedCells }
                }
                if (overlaps) displacedTileIds.add(tile.id)
            }
        }

        if (displacedTileIds.isEmpty() && displacedGroupIds.isEmpty()) return tiles

        // Build occupancy from non-displaced items
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val processedGroups = mutableSetOf<String>()
        for (tile in tiles) {
            if (tile.gridCol < 0 || tile.gridRow < 0) continue
            if (tile.id in displacedTileIds) continue
            val gid = tile.groupId
            if (gid != null && gid in displacedGroupIds) continue

            if (gid != null) {
                if (gid in processedGroups) continue
                processedGroups.add(gid)
                for (c in tile.gridCol until tile.gridCol + 2) {
                    for (r in tile.gridRow until tile.gridRow + 2) {
                        occupied.add(c to r)
                    }
                }
            } else {
                for (c in tile.gridCol until tile.gridCol + tile.columnSpan) {
                    for (r in tile.gridRow until tile.gridRow + tile.rowSpan) {
                        occupied.add(c to r)
                    }
                }
            }
        }

        var result = tiles
        for (dId in displacedTileIds) {
            val t = result.find { it.id == dId } ?: continue
            val (nc, nr) = findNextAvailableCellFromSet(occupied, t.columnSpan, t.rowSpan)
            result = result.map { if (it.id == dId) it.copy(gridCol = nc, gridRow = nr) else it }
            for (c in nc until nc + t.columnSpan) {
                for (r in nr until nr + t.rowSpan) { occupied.add(c to r) }
            }
        }
        for (gId in displacedGroupIds) {
            val (nc, nr) = findNextAvailableCellFromSet(occupied, 2, 2)
            result = result.map { if (it.groupId == gId) it.copy(gridCol = nc, gridRow = nr) else it }
            for (c in nc until nc + 2) {
                for (r in nr until nr + 2) { occupied.add(c to r) }
            }
        }
        return result
    }

    // ─────────────── Group operations ───────────────

    /**
     * Create a new group from two tiles. The dragged tile is merged into
     * the target tile's position. Both tiles get the same groupId.
     */
    fun createGroup(tileId1: String, tileId2: String) {
        val groupId = UUID.randomUUID().toString()
        val current = _tiles.value
        val target = current.find { it.id == tileId2 } ?: return
        val groupName = "Group"

        _tiles.value = current.map { tile ->
            when (tile.id) {
                tileId2 -> tile.copy(
                    groupId = groupId,
                    groupName = groupName,
                    columnSpan = 2,
                    rowSpan = 2
                )
                tileId1 -> tile.copy(
                    groupId = groupId,
                    position = target.position,
                    gridCol = target.gridCol,
                    gridRow = target.gridRow,
                    columnSpan = 2,
                    rowSpan = 2
                )
                else -> tile
            }
        }.sortedBy { it.position }
    }

    /** Add an existing tile to a group. */
    fun addToGroup(tileId: String, groupId: String) {
        val groupTiles = _tiles.value.filter { it.groupId == groupId }
        val first = groupTiles.firstOrNull() ?: return
        _tiles.value = _tiles.value.map { tile ->
            if (tile.id == tileId) tile.copy(
                groupId = groupId,
                position = first.position,
                gridCol = first.gridCol,
                gridRow = first.gridRow,
                columnSpan = 2,
                rowSpan = 2
            ) else tile
        }.sortedBy { it.position }
    }

    /** Rename a group. Updates groupName on all tiles in the group. */
    fun renameGroup(groupId: String, newName: String) {
        _tiles.value = _tiles.value.map { tile ->
            if (tile.groupId == groupId) tile.copy(groupName = newName)
            else tile
        }
    }

    /** Resize the group header tile. Updates the first tile's groupHeader spans. */
    fun setGroupSpans(groupId: String, colSpan: Int, rowSpan: Int) {
        val groupTiles = _tiles.value.filter { it.groupId == groupId }.sortedBy { it.position }
        val first = groupTiles.firstOrNull() ?: return
        val cCol = colSpan.coerceIn(1, gridColumns)
        val cRow = rowSpan.coerceIn(1, 4)
        _tiles.value = _tiles.value.map { tile ->
            if (tile.id == first.id) tile.copy(groupHeaderColSpan = cCol, groupHeaderRowSpan = cRow)
            else tile
        }
    }

    /** Remove a tile from its group (ungroup). */
    fun ungroupTile(tileId: String) {
        val current = _tiles.value
        val tile = current.find { it.id == tileId } ?: return
        val gid = tile.groupId ?: return

        // Find an available grid position for the ungrouped tile
        val otherTiles = current.filter { it.id != tileId }
        val (newCol, newRow) = findNextAvailableCell(otherTiles, tile.columnSpan, tile.rowSpan)

        // Remove from group and assign new grid position
        val updated = current.map {
            if (it.id == tileId) it.copy(groupId = null, groupName = null, gridCol = newCol, gridRow = newRow) else it
        }

        // If only one tile remains in the group, ungroup it too
        val remaining = updated.filter { it.groupId == gid }
        _tiles.value = if (remaining.size <= 1) {
            updated.map {
                if (it.groupId == gid) it.copy(groupId = null, groupName = null) else it
            }
        } else {
            updated
        }.sortedBy { it.position }
    }

    /**
     * Swap the order of two tiles within the same group.
     * Uses list-index ordering within the group subset.
     */
    fun swapGroupTiles(tileId1: String, tileId2: String) {
        val current = _tiles.value
        val tile1 = current.find { it.id == tileId1 } ?: return
        val tile2 = current.find { it.id == tileId2 } ?: return
        // Both must be in the same group
        if (tile1.groupId == null || tile1.groupId != tile2.groupId) return

        // Swap their list positions
        val idx1 = current.indexOf(tile1)
        val idx2 = current.indexOf(tile2)
        if (idx1 >= 0 && idx2 >= 0) {
            val mutable = current.toMutableList()
            mutable[idx1] = tile2.copy(position = tile1.position)
            mutable[idx2] = tile1.copy(position = tile2.position)
            _tiles.value = mutable.sortedBy { it.position }
        }
    }

    /**
     * Move a group tile to a specific grid position within its expanded group.
     * Rejects the move if the target position would overlap another tile.
     */
    fun moveGroupTile(tileId: String, targetCol: Int, targetRow: Int) {
        val current = _tiles.value
        val movingTile = current.find { it.id == tileId } ?: return
        val groupId = movingTile.groupId ?: return

        // Clamp target within grid bounds
        val col = targetCol.coerceIn(0, (gridColumns - movingTile.columnSpan).coerceAtLeast(0))
        val row = targetRow.coerceAtLeast(0)

        // Check overlap with other tiles in the same group
        val siblings = current.filter { it.groupId == groupId && it.id != tileId }
        val overlaps = siblings.any { other ->
            val oCol = if (other.groupCol >= 0) other.groupCol else return@any false
            val oRow = if (other.groupRow >= 0) other.groupRow else return@any false
            col < oCol + other.columnSpan &&
            col + movingTile.columnSpan > oCol &&
            row < oRow + other.rowSpan &&
            row + movingTile.rowSpan > oRow
        }

        if (!overlaps) {
            _tiles.value = current.map { tile ->
                if (tile.id == tileId) tile.copy(groupCol = col, groupRow = row)
                else tile
            }
        }
    }

    /** Toggle expanded/collapsed state of a group. */
    fun toggleGroupExpanded(groupId: String) {
        val current = _expandedGroups.value.toMutableSet()
        if (groupId in current) current.remove(groupId) else current.add(groupId)
        _expandedGroups.value = current
    }

    /**
     * Compact the grid by shifting all tiles upward to remove empty rows.
     * Each item is moved to the earliest row where it fits without overlapping
     * already-placed items, preserving column positions and relative ordering.
     */
    fun compactGrid() {
        val current = _tiles.value
        if (current.isEmpty()) return

        // Collect grid items: standalone tiles and groups (deduplicated)
        data class GridEntry(
            val key: String,        // tileId or groupId
            val isGroup: Boolean,
            val col: Int,
            val row: Int,
            val colSpan: Int,
            val rowSpan: Int
        )

        val entries = mutableListOf<GridEntry>()
        val seenGroups = mutableSetOf<String>()

        for (tile in current) {
            if (tile.gridCol < 0 || tile.gridRow < 0) continue
            val gid = tile.groupId
            if (gid != null) {
                if (gid in seenGroups) continue
                seenGroups.add(gid)
                entries.add(GridEntry(gid, true, tile.gridCol, tile.gridRow, 2, 2))
            } else {
                entries.add(GridEntry(tile.id, false, tile.gridCol, tile.gridRow, tile.columnSpan, tile.rowSpan))
            }
        }

        // Sort by row first (top→bottom), then column (left→right) for stable compaction
        entries.sortWith(compareBy({ it.row }, { it.col }))

        // Place each entry at the highest possible row, keeping its column
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val newPositions = mutableMapOf<String, Pair<Int, Int>>() // key → (col, row)

        for (entry in entries) {
            val col = entry.col
            // Find the first row where this item fits at its current column
            var bestRow = 0
            for (r in 0..200) {
                val fits = (0 until entry.colSpan).all { dc ->
                    (0 until entry.rowSpan).all { dr ->
                        (col + dc to r + dr) !in occupied
                    }
                }
                if (fits) {
                    bestRow = r
                    break
                }
            }
            // Mark occupied cells
            for (dc in 0 until entry.colSpan) {
                for (dr in 0 until entry.rowSpan) {
                    occupied.add(col + dc to bestRow + dr)
                }
            }
            newPositions[entry.key] = col to bestRow
        }

        // Apply new positions
        val seenGroupsApply = mutableSetOf<String>()
        _tiles.value = current.map { tile ->
            val gid = tile.groupId
            if (gid != null) {
                val pos = newPositions[gid]
                if (pos != null) tile.copy(gridCol = pos.first, gridRow = pos.second) else tile
            } else {
                val pos = newPositions[tile.id]
                if (pos != null) tile.copy(gridCol = pos.first, gridRow = pos.second) else tile
            }
        }
    }

    // ─────────────── Grid position helpers ───────────────

    /**
     * Build a set of occupied grid cells from the current tile list.
     * Grouped tiles share the same grid position — only counted once per group.
     * Can exclude a specific tile or group so we don't collide with ourselves.
     */
    private fun buildOccupancySet(
        tiles: List<Tile>,
        excludeTileId: String? = null,
        excludeGroupId: String? = null
    ): Set<Pair<Int, Int>> {
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val seenGroups = mutableSetOf<String>()

        for (tile in tiles) {
            if (tile.id == excludeTileId) continue
            if (tile.gridCol < 0 || tile.gridRow < 0) continue

            val gid = tile.groupId
            if (gid != null) {
                if (gid == excludeGroupId) continue
                if (gid in seenGroups) continue  // only count group once
                seenGroups.add(gid)
                // Groups occupy 2×2 on the main grid
                for (c in tile.gridCol until tile.gridCol + 2) {
                    for (r in tile.gridRow until tile.gridRow + 2) {
                        occupied.add(c to r)
                    }
                }
            } else {
                for (c in tile.gridCol until tile.gridCol + tile.columnSpan) {
                    for (r in tile.gridRow until tile.gridRow + tile.rowSpan) {
                        occupied.add(c to r)
                    }
                }
            }
        }
        return occupied
    }

    /** Check if a tile of given span can be placed at (col, row) without overlap. */
    private fun canPlace(
        occupied: Set<Pair<Int, Int>>,
        col: Int, row: Int,
        colSpan: Int, rowSpan: Int
    ): Boolean {
        for (c in col until col + colSpan) {
            for (r in row until row + rowSpan) {
                if ((c to r) in occupied) return false
            }
        }
        return true
    }

    /** Auto-assign grid positions to tiles that don't have them (-1). */
    private fun autoAssignGridPositions(tiles: List<Tile>): List<Tile> {
        val occupied = mutableSetOf<Pair<Int, Int>>()
        val seenGroups = mutableSetOf<String>()

        // First pass: mark cells occupied by tiles that already have positions
        for (tile in tiles) {
            if (tile.gridCol >= 0 && tile.gridRow >= 0) {
                val gid = tile.groupId
                if (gid != null) {
                    if (gid in seenGroups) continue
                    seenGroups.add(gid)
                    // Groups are 2×2 on the main grid
                    for (c in tile.gridCol until tile.gridCol + 2) {
                        for (r in tile.gridRow until tile.gridRow + 2) {
                            occupied.add(c to r)
                        }
                    }
                } else {
                    for (c in tile.gridCol until tile.gridCol + tile.columnSpan) {
                        for (r in tile.gridRow until tile.gridRow + tile.rowSpan) {
                            occupied.add(c to r)
                        }
                    }
                }
            }
        }

        val assignedGroups = mutableSetOf<String>()

        // Second pass: assign positions to tiles without them
        return tiles.map { tile ->
            if (tile.gridCol >= 0 && tile.gridRow >= 0) tile
            else {
                val gid = tile.groupId
                if (gid != null && gid in assignedGroups) {
                    // Already assigned this group's position — use same coords
                    val groupTile = tiles.find { it.groupId == gid && it.gridCol >= 0 && it.gridRow >= 0 }
                    if (groupTile != null) {
                        tile.copy(gridCol = groupTile.gridCol, gridRow = groupTile.gridRow)
                    } else tile
                } else {
                    val span = if (gid != null) 2 else tile.columnSpan
                    val rSpan = if (gid != null) 2 else tile.rowSpan
                    val (col, row) = findNextAvailableCellFromSet(occupied, span, rSpan)
                    for (c in col until col + span) {
                        for (r in row until row + rSpan) {
                            occupied.add(c to r)
                        }
                    }
                    if (gid != null) assignedGroups.add(gid)
                    tile.copy(gridCol = col, gridRow = row)
                }
            }
        }
    }

    /** Find the next empty grid cell that can fit a tile of the given span. */
    private fun findNextAvailableCell(existingTiles: List<Tile>, colSpan: Int, rowSpan: Int): Pair<Int, Int> {
        val occupied = buildOccupancySet(existingTiles)
        return findNextAvailableCellFromSet(occupied, colSpan, rowSpan)
    }

    private fun findNextAvailableCellFromSet(
        occupied: Set<Pair<Int, Int>>,
        colSpan: Int,
        rowSpan: Int
    ): Pair<Int, Int> {
        for (row in 0..200) {
            for (col in 0..(gridColumns - colSpan)) {
                val canFit = (0 until colSpan).all { dc ->
                    (0 until rowSpan).all { dr ->
                        (col + dc to row + dr) !in occupied
                    }
                }
                if (canFit) return col to row
            }
        }
        return 0 to 0 // fallback
    }
}
