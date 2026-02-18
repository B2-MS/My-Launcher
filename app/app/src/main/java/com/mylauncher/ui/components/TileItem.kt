package com.mylauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.mylauncher.data.model.Tile
import com.mylauncher.data.model.TileSize

/**
 * A single Metro-style tile composable.
 */
@Composable
fun TileItem(
    tile: Tile,
    appIcon: Drawable?,
    accentColor: Color,
    tileOpacity: Float,
    columnWidth: Dp,
    gap: Dp,
    isEditMode: Boolean,
    onTileTap: () -> Unit,
    onTileLongPress: () -> Unit,
    onUnpin: () -> Unit,
    onResize: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileColor = if (tile.colorOverride != null) {
        Color(tile.colorOverride)
    } else {
        accentColor
    }
    val opacity = tile.transparencyOverride ?: tileOpacity
    val tileWidth = columnWidth * tile.size.columnSpan + gap * (tile.size.columnSpan - 1)
    val tileHeight = columnWidth * tile.size.rowSpan + gap * (tile.size.rowSpan - 1)

    // Edit mode shrink animation
    val editScale by animateFloatAsState(
        targetValue = if (isEditMode) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "editScale"
    )

    // Tilt animation on press
    var isTilted by remember { mutableStateOf(false) }
    val tiltAngle by animateFloatAsState(
        targetValue = if (isTilted) 3f else 0f,
        animationSpec = tween(durationMillis = 100),
        label = "tilt"
    )

    Box(
        modifier = modifier
            .width(tileWidth)
            .height(tileHeight)
            .scale(editScale)
            .graphicsLayer {
                rotationX = tiltAngle
                cameraDistance = 12f * density
            }
            .clip(RoundedCornerShape(2.dp))
            .background(tileColor.copy(alpha = opacity))
            .pointerInput(isEditMode) {
                detectTapGestures(
                    onPress = {
                        isTilted = true
                        try {
                            awaitRelease()
                        } finally {
                            isTilted = false
                        }
                    },
                    onTap = {
                        if (isEditMode) {
                            onResize()
                        } else {
                            onTileTap()
                        }
                    },
                    onLongPress = {
                        onTileLongPress()
                    }
                )
            }
    ) {
        // App Icon - centered
        if (appIcon != null) {
            val iconSize = when (tile.size) {
                TileSize.SMALL -> 24.dp
                TileSize.MEDIUM -> 40.dp
                TileSize.WIDE -> 40.dp
            }

            val bitmap = remember(appIcon) {
                appIcon.toBitmap(
                    width = 128,
                    height = 128
                ).asImageBitmap()
            }

            Image(
                bitmap = bitmap,
                contentDescription = tile.appName,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.Center)
                    .then(
                        if (tile.size != TileSize.SMALL)
                            Modifier.offset(y = (-8).dp)
                        else Modifier
                    )
            )
        }

        // App Name — bottom-left (hidden for Small tiles)
        if (tile.size != TileSize.SMALL) {
            Text(
                text = tile.appName,
                color = Color.White,
                fontSize = when (tile.size) {
                    TileSize.MEDIUM -> 12.sp
                    TileSize.WIDE -> 13.sp
                    else -> 11.sp
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 6.dp, bottom = 4.dp, end = 6.dp)
            )
        }

        // Unpin button in edit mode
        if (isEditMode) {
            IconButton(
                onClick = onUnpin,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Unpin",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Size label in edit mode
            Text(
                text = tile.size.name,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            )
        }
    }
}
