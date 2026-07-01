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
import com.mylauncher.service.LiveTileInfo

/**
 * A single Metro-style tile composable.
 */
@Composable
fun TileItem(
    tile: Tile,
    appIcon: Drawable?,
    accentColor: Color,
    tileOpacity: Float,
    bevelEnabled: Boolean,
    bevelDepth: Float,
    columnWidth: Dp,
    gap: Dp,
    isEditMode: Boolean,
    isSelectedForMove: Boolean = false,
    onTileTap: () -> Unit,
    onTileLongPress: () -> Unit,
    onUnpin: () -> Unit,
    liveTileInfo: LiveTileInfo? = null,
    modifier: Modifier = Modifier
) {
    val tileColor = if (tile.colorOverride != null) {
        Color(tile.colorOverride)
    } else {
        accentColor
    }
    val opacity = tile.transparencyOverride ?: tileOpacity
    val tileWidth = columnWidth * tile.columnSpan + gap * (tile.columnSpan - 1)
    val tileHeight = columnWidth * tile.rowSpan + gap * (tile.rowSpan - 1)

    val isSmall = tile.columnSpan <= 1 && tile.rowSpan <= 1

    // Edit mode shrink animation — selected tile is slightly larger to show it's "picked up"
    val editScale by animateFloatAsState(
        targetValue = when {
            isSelectedForMove -> 0.96f
            isEditMode -> 0.92f
            else -> 1f
        },
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

    // Use rememberUpdatedState so the gesture detector always sees the latest callbacks
    val currentOnTileTap by rememberUpdatedState(onTileTap)
    val currentOnTileLongPress by rememberUpdatedState(onTileLongPress)

    Box(
        modifier = modifier
            .width(tileWidth)
            .height(tileHeight)
            .scale(editScale)
            .graphicsLayer {
                rotationX = tiltAngle
                cameraDistance = 12f * density
            }
            .then(
                if (isSelectedForMove) Modifier.border(
                    width = 2.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(2.dp)
                ) else Modifier
            )
            .clip(RoundedCornerShape(2.dp))
            .background(tileColor.copy(alpha = opacity))
            .tileBevel(bevelEnabled, bevelDepth)
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
                    onTap = { currentOnTileTap() },
                    onLongPress = null   // parent's detectDragGesturesAfterLongPress handles this
                )
            }
    ) {
        // App Icon - centered
        if (appIcon != null) {
            val iconSize = if (isSmall) 36.dp else 60.dp

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
                        if (!isSmall)
                            Modifier.offset(y = (-8).dp)
                        else Modifier
                    )
            )
        }

        // App Name — bottom-left (hidden for small tiles)
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

        // Live tile badge + info overlay
        if (liveTileInfo != null && liveTileInfo.badgeCount > 0 && !isEditMode) {
            // Badge count — top right
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(if (liveTileInfo.badgeCount >= 100) 26.dp else 22.dp)
                    .background(Color.White, RoundedCornerShape(50))
            ) {
                Text(
                    text = if (liveTileInfo.badgeCount > 99) "99+" else liveTileInfo.badgeCount.toString(),
                    color = tileColor,
                    fontSize = if (liveTileInfo.badgeCount >= 100) 9.sp else 11.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    maxLines = 1
                )
            }

            // Live tile text lines for tall tiles (≥ 2×2) — top left
            if (tile.columnSpan >= 2 && tile.rowSpan >= 2) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 6.dp, top = 6.dp, end = 30.dp)
                ) {
                    if (liveTileInfo.line1 != null) {
                        Text(
                            text = liveTileInfo.line1,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (liveTileInfo.line2 != null) {
                        Text(
                            text = liveTileInfo.line2,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            // Live tile text for wide but short tiles (≥ 4 wide, 1 row) — right of center
            else if (tile.columnSpan >= 4 && tile.rowSpan <= 1) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 30.dp)
                        .fillMaxWidth(0.5f),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (liveTileInfo.line1 != null) {
                        Text(
                            text = liveTileInfo.line1,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (liveTileInfo.line2 != null) {
                        Text(
                            text = liveTileInfo.line2,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
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
