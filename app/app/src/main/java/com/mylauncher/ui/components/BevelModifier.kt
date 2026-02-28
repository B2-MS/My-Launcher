package com.mylauncher.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable bevel modifier — draws glass-like gradient edges on a composable.
 */
fun Modifier.tileBevel(enabled: Boolean, depth: Float): Modifier {
    if (!enabled || depth <= 0f) return this
    return this.drawWithContent {
        drawContent()
        val bevelWidth = (2.dp.toPx()) * depth
        // Top highlight
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White.copy(alpha = 0.25f * depth.coerceAtMost(1f)), Color.Transparent),
                startY = 0f, endY = bevelWidth * 3
            ),
            size = size.copy(height = bevelWidth * 3)
        )
        // Left highlight
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.White.copy(alpha = 0.18f * depth.coerceAtMost(1f)), Color.Transparent),
                startX = 0f, endX = bevelWidth * 3
            ),
            size = size.copy(width = bevelWidth * 3)
        )
        // Bottom shadow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.20f * depth.coerceAtMost(1f))),
                startY = size.height - bevelWidth * 3, endY = size.height
            ),
            topLeft = Offset(0f, size.height - bevelWidth * 3),
            size = size.copy(height = bevelWidth * 3)
        )
        // Right shadow
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f * depth.coerceAtMost(1f))),
                startX = size.width - bevelWidth * 3, endX = size.width
            ),
            topLeft = Offset(size.width - bevelWidth * 3, 0f),
            size = size.copy(width = bevelWidth * 3)
        )
    }
}
