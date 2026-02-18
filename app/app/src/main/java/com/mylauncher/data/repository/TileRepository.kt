package com.mylauncher.data.repository

import com.mylauncher.data.model.Tile
import com.mylauncher.data.model.TileSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun setTiles(tiles: List<Tile>) {
        _tiles.value = tiles.sortedBy { it.position }
    }

    fun addTile(tile: Tile) {
        val current = _tiles.value.toMutableList()
        val newPosition = (current.maxOfOrNull { it.position } ?: -1) + 1
        current.add(tile.copy(position = newPosition))
        _tiles.value = current
    }

    fun removeTile(tileId: String) {
        _tiles.value = _tiles.value.filter { it.id != tileId }
    }

    fun updateTile(tile: Tile) {
        _tiles.value = _tiles.value.map { if (it.id == tile.id) tile else it }
    }

    fun resizeTile(tileId: String) {
        _tiles.value = _tiles.value.map { tile ->
            if (tile.id == tileId) {
                val nextSize = when (tile.size) {
                    TileSize.SMALL -> TileSize.MEDIUM
                    TileSize.MEDIUM -> TileSize.WIDE
                    TileSize.WIDE -> TileSize.SMALL
                }
                tile.copy(size = nextSize)
            } else tile
        }
    }

    fun moveTile(fromIndex: Int, toIndex: Int) {
        val current = _tiles.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _tiles.value = current.mapIndexed { index, tile -> tile.copy(position = index) }
        }
    }
}
