package com.mylauncher.ui.components

import android.appwidget.AppWidgetHostView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mylauncher.data.model.Tile
import com.mylauncher.widget.LauncherWidgetHost

/**
 * Renders an Android AppWidget inside the tile grid.
 * Uses AndroidView to host the native AppWidgetHostView.
 *
 * In edit mode, an overlay intercepts all touches so the user can:
 *   - Tap the tile to open the resize/settings dialog
 *   - Tap the X to remove the widget
 */
@Composable
fun WidgetTileItem(
    tile: Tile,
    widgetHost: LauncherWidgetHost,
    columnWidth: Dp,
    gap: Dp,
    isEditMode: Boolean,
    onEditTap: () -> Unit,
    onUnpin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tileWidth = columnWidth * tile.columnSpan + gap * (tile.columnSpan - 1)
    val tileHeight = columnWidth * tile.rowSpan + gap * (tile.rowSpan - 1)

    val widgetInfo = remember(tile.appWidgetId) {
        widgetHost.getWidgetInfo(tile.appWidgetId)
    }

    Box(
        modifier = modifier
            .width(tileWidth)
            .height(tileHeight)
            .scale(if (isEditMode) 0.92f else 1f)
            .clip(RoundedCornerShape(2.dp))
            .then(
                if (isEditMode) Modifier.border(
                    1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(2.dp)
                ) else Modifier
            )
    ) {
        if (widgetInfo != null) {
            AndroidView(
                factory = { ctx ->
                    try {
                        widgetHost.createView(tile.appWidgetId).apply {
                            setAppWidget(tile.appWidgetId, widgetInfo)
                        }
                    } catch (e: Exception) {
                        android.widget.FrameLayout(ctx).apply {
                            setBackgroundColor(android.graphics.Color.argb(80, 255, 255, 255))
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Widget unavailable",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Edit mode: translucent overlay intercepts all touches
        // so the native widget doesn't consume them.
        // Use detectTapGestures (not .clickable) so long-press propagates
        // to the parent's drag gesture for tile reordering.
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onEditTap() })
                    }
            ) {
                // Size badge top-left
                Text(
                    text = "${tile.columnSpan}\u00D7${tile.rowSpan}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )

                // Resize hint
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Resize widget",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp)
                )

                // Remove button top-right
                IconButton(
                    onClick = onUnpin,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove widget",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Non-edit mode: transparent overlay prevents the native AndroidView
        // from stealing touch events.  Tap does nothing (widget is
        // display-only); long-press falls through to the parent's
        // detectDragGesturesAfterLongPress so the user can enter edit mode.
        if (!isEditMode) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        detectTapGestures { /* tap — no-op */ }
                    }
            )
        }
    }
}
