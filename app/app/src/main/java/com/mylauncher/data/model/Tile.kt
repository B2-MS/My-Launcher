package com.mylauncher.data.model

import java.util.UUID

/**
 * Represents a single tile on the Start screen.
 * Column/row spans are freely adjustable (1–6 columns, 1–4 rows)
 * relative to the grid.
 */
data class Tile(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val appName: String,
    val columnSpan: Int = 2,                  // width in grid columns (1..6)
    val rowSpan: Int = 2,                     // height in grid rows (1..4)
    val position: Int = 0,                    // order position in the grid
    val colorOverride: Long? = null,          // per-tile accent color (ARGB), null = use global
    val transparencyOverride: Float? = null,  // per-tile opacity 0f..1f, null = use global
    val isLiveTile: Boolean = false,
    val liveContent: String? = null,          // future: serialised live tile data
    val groupId: String? = null,              // non-null = this tile belongs to a group
    val groupName: String? = null,            // display name for group (set on first tile only)
    val groupCol: Int = -1,                   // column offset within expanded group (-1 = auto)
    val groupRow: Int = -1,                   // row offset within expanded group (-1 = auto)
    val gridCol: Int = -1,                    // column position in the main grid (-1 = auto)
    val gridRow: Int = -1,                    // row position in the main grid (-1 = auto)
    val appWidgetId: Int = 0,                 // non-zero = this tile hosts an Android widget
    val groupHeaderColSpan: Int = 2,          // group header tile width (used on first tile only)
    val groupHeaderRowSpan: Int = 2,          // group header tile height (used on first tile only)
    val userSerialNumber: Long = 0L           // user profile serial (0 = personal, else work)
) {
    val isWidget: Boolean get() = appWidgetId != 0
}

/**
 * Helper to extract logical groups from a flat tile list.
 * Returns tiles in order, collapsing grouped tiles into a [TileGroup].
 */
data class TileGroup(
    val groupId: String,
    val name: String,
    val tiles: List<Tile>,
    val columnSpan: Int,
    val rowSpan: Int,
    val position: Int,
    val gridCol: Int = 0,
    val gridRow: Int = 0
)

sealed class GridItem {
    abstract val gridCol: Int
    abstract val gridRow: Int
    abstract val colSpan: Int
    abstract val rowSpan: Int

    data class Single(val tile: Tile) : GridItem() {
        override val gridCol get() = tile.gridCol
        override val gridRow get() = tile.gridRow
        override val colSpan get() = tile.columnSpan
        override val rowSpan get() = tile.rowSpan
    }
    data class Group(val group: TileGroup) : GridItem() {
        override val gridCol get() = group.gridCol
        override val gridRow get() = group.gridRow
        override val colSpan get() = group.columnSpan
        override val rowSpan get() = group.rowSpan
    }
}

fun buildGridItems(tiles: List<Tile>): List<GridItem> {
    val sorted = tiles.sortedBy { it.position }
    val grouped = mutableMapOf<String, MutableList<Tile>>()
    val items = mutableListOf<GridItem>()
    val seenGroups = mutableSetOf<String>()

    for (tile in sorted) {
        val gid = tile.groupId
        if (gid != null) {
            grouped.getOrPut(gid) { mutableListOf() }.add(tile)
            if (gid !in seenGroups) {
                seenGroups.add(gid)
                // Placeholder — will be replaced after we collect all tiles
                items.add(GridItem.Group(TileGroup(gid, "", emptyList(), 2, 2, tile.position, tile.gridCol, tile.gridRow)))
            }
        } else {
            items.add(GridItem.Single(tile))
        }
    }

    // Replace placeholders with full groups
    return items.map { item ->
        when (item) {
            is GridItem.Single -> item
            is GridItem.Group -> {
                val groupTiles = grouped[item.group.groupId] ?: emptyList()
                val first = groupTiles.first()
                GridItem.Group(
                    TileGroup(
                        groupId = item.group.groupId,
                        name = first.groupName ?: "Group",
                        tiles = groupTiles,
                        columnSpan = first.groupHeaderColSpan,
                        rowSpan = first.groupHeaderRowSpan,
                        position = first.position,
                        gridCol = first.gridCol,
                        gridRow = first.gridRow
                    )
                )
            }
        }
    }
}
