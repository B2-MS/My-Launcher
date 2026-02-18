package com.mylauncher.data.model

import java.util.UUID

/**
 * Tile sizes matching Windows Phone: Small (1×1), Medium (2×2), Wide (4×2).
 * Column spans are relative to a 6-column grid.
 */
enum class TileSize(val columnSpan: Int, val rowSpan: Int) {
    SMALL(1, 1),
    MEDIUM(2, 2),
    WIDE(4, 2)
}

/**
 * Represents a single tile on the Start screen.
 */
data class Tile(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val appName: String,
    val size: TileSize = TileSize.MEDIUM,
    val position: Int = 0,                    // order position in the grid
    val colorOverride: Long? = null,          // per-tile accent color (ARGB), null = use global
    val transparencyOverride: Float? = null,  // per-tile opacity 0f..1f, null = use global
    val isLiveTile: Boolean = false,
    val liveContent: String? = null           // future: serialised live tile data
)
