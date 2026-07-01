package com.mylauncher.data.model

import org.json.JSONArray
import org.json.JSONObject

/**
 * A saved theme = snapshot of the tile layout + visual preferences.
 * Serialised as JSON for DataStore storage.
 */
data class SavedTheme(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val accentColorArgb: Long,
    val globalTileOpacity: Float,
    val tileAnimationIntervalMs: Long,
    val bevelEnabled: Boolean = true,
    val bevelDepth: Float = 1f,
    val darkModeEnabled: Boolean = true,
    val wallpaperOnlyInTiles: Boolean = false,
    val tiles: List<TileSnapshot>
)

/**
 * Lightweight snapshot of a tile's layout-relevant properties.
 */
data class TileSnapshot(
    val packageName: String,
    val appName: String,
    val columnSpan: Int,
    val rowSpan: Int,
    val position: Int,
    val colorOverride: Long? = null,
    val transparencyOverride: Float? = null,
    val isLiveTile: Boolean = false,
    val groupId: String?,
    val groupName: String?,
    val groupCol: Int = -1,
    val groupRow: Int = -1,
    val gridCol: Int = -1,
    val gridRow: Int = -1,
    val groupHeaderColSpan: Int = 2,
    val groupHeaderRowSpan: Int = 2,
    val userSerialNumber: Long = 0L
)

// ─────────────── JSON serialisation ───────────────

fun SavedTheme.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("createdAt", createdAt)
    put("accentColorArgb", accentColorArgb)
    put("globalTileOpacity", globalTileOpacity.toDouble())
    put("tileAnimationIntervalMs", tileAnimationIntervalMs)
    put("bevelEnabled", bevelEnabled)
    put("bevelDepth", bevelDepth.toDouble())
    put("darkModeEnabled", darkModeEnabled)
    put("wallpaperOnlyInTiles", wallpaperOnlyInTiles)
    put("tiles", JSONArray().apply {
        for (t in tiles) {
            put(JSONObject().apply {
                put("packageName", t.packageName)
                put("appName", t.appName)
                put("columnSpan", t.columnSpan)
                put("rowSpan", t.rowSpan)
                put("position", t.position)
                if (t.colorOverride != null) put("colorOverride", t.colorOverride)
                if (t.transparencyOverride != null) put("transparencyOverride", t.transparencyOverride.toDouble())
                put("isLiveTile", t.isLiveTile)
                if (t.groupId != null) put("groupId", t.groupId)
                if (t.groupName != null) put("groupName", t.groupName)
                put("groupCol", t.groupCol)
                put("groupRow", t.groupRow)
                put("gridCol", t.gridCol)
                put("gridRow", t.gridRow)
                put("groupHeaderColSpan", t.groupHeaderColSpan)
                put("groupHeaderRowSpan", t.groupHeaderRowSpan)
                put("userSerialNumber", t.userSerialNumber)
            })
        }
    })
}

fun JSONObject.toSavedTheme(): SavedTheme {
    val tilesArray = getJSONArray("tiles")
    val tiles = (0 until tilesArray.length()).map { i ->
        val t = tilesArray.getJSONObject(i)
        TileSnapshot(
            packageName = t.getString("packageName"),
            appName = t.getString("appName"),
            columnSpan = t.getInt("columnSpan"),
            rowSpan = t.getInt("rowSpan"),
            position = t.getInt("position"),
            colorOverride = if (t.has("colorOverride")) t.getLong("colorOverride") else null,
            transparencyOverride = if (t.has("transparencyOverride")) t.getDouble("transparencyOverride").toFloat() else null,
            isLiveTile = if (t.has("isLiveTile")) t.getBoolean("isLiveTile") else false,
            groupId = if (t.has("groupId")) t.getString("groupId") else null,
            groupName = if (t.has("groupName")) t.getString("groupName") else null,
            groupCol = if (t.has("groupCol")) t.getInt("groupCol") else -1,
            groupRow = if (t.has("groupRow")) t.getInt("groupRow") else -1,
            gridCol = if (t.has("gridCol")) t.getInt("gridCol") else -1,
            gridRow = if (t.has("gridRow")) t.getInt("gridRow") else -1,
            groupHeaderColSpan = if (t.has("groupHeaderColSpan")) t.getInt("groupHeaderColSpan") else 2,
            groupHeaderRowSpan = if (t.has("groupHeaderRowSpan")) t.getInt("groupHeaderRowSpan") else 2,
            userSerialNumber = if (t.has("userSerialNumber")) t.getLong("userSerialNumber") else 0L
        )
    }
    return SavedTheme(
        id = getString("id"),
        name = getString("name"),
        createdAt = getLong("createdAt"),
        accentColorArgb = getLong("accentColorArgb"),
        globalTileOpacity = getDouble("globalTileOpacity").toFloat(),
        tileAnimationIntervalMs = getLong("tileAnimationIntervalMs"),
        bevelEnabled = if (has("bevelEnabled")) getBoolean("bevelEnabled") else true,
        bevelDepth = if (has("bevelDepth")) getDouble("bevelDepth").toFloat() else 1f,
        darkModeEnabled = if (has("darkModeEnabled")) getBoolean("darkModeEnabled") else true,
        wallpaperOnlyInTiles = if (has("wallpaperOnlyInTiles")) getBoolean("wallpaperOnlyInTiles") else false,
        tiles = tiles
    )
}

fun List<SavedTheme>.toJsonString(): String =
    JSONArray().apply { for (t in this@toJsonString) put(t.toJson()) }.toString()

fun String.toSavedThemes(): List<SavedTheme> {
    if (isBlank()) return emptyList()
    val array = JSONArray(this)
    return (0 until array.length()).map { array.getJSONObject(it).toSavedTheme() }
}
