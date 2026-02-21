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
    val groupId: String?,
    val groupName: String?,
    val groupCol: Int = -1,
    val groupRow: Int = -1,
    val gridCol: Int = -1,
    val gridRow: Int = -1
)

// ─────────────── JSON serialisation ───────────────

fun SavedTheme.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("createdAt", createdAt)
    put("accentColorArgb", accentColorArgb)
    put("globalTileOpacity", globalTileOpacity.toDouble())
    put("tileAnimationIntervalMs", tileAnimationIntervalMs)
    put("tiles", JSONArray().apply {
        for (t in tiles) {
            put(JSONObject().apply {
                put("packageName", t.packageName)
                put("appName", t.appName)
                put("columnSpan", t.columnSpan)
                put("rowSpan", t.rowSpan)
                put("position", t.position)
                if (t.groupId != null) put("groupId", t.groupId)
                if (t.groupName != null) put("groupName", t.groupName)
                put("groupCol", t.groupCol)
                put("groupRow", t.groupRow)
                put("gridCol", t.gridCol)
                put("gridRow", t.gridRow)
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
            groupId = if (t.has("groupId")) t.getString("groupId") else null,
            groupName = if (t.has("groupName")) t.getString("groupName") else null,
            groupCol = if (t.has("groupCol")) t.getInt("groupCol") else -1,
            groupRow = if (t.has("groupRow")) t.getInt("groupRow") else -1,
            gridCol = if (t.has("gridCol")) t.getInt("gridCol") else -1,
            gridRow = if (t.has("gridRow")) t.getInt("gridRow") else -1
        )
    }
    return SavedTheme(
        id = getString("id"),
        name = getString("name"),
        createdAt = getLong("createdAt"),
        accentColorArgb = getLong("accentColorArgb"),
        globalTileOpacity = getDouble("globalTileOpacity").toFloat(),
        tileAnimationIntervalMs = getLong("tileAnimationIntervalMs"),
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
