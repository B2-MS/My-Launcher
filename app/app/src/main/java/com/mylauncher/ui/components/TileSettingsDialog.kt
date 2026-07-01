package com.mylauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mylauncher.data.model.Tile
import kotlin.math.roundToInt

/**
 * Dialog for configuring a tile — width/height sliders and live-tile toggle.
 * Shown when tapping a tile in edit mode.
 */
@Composable
fun TileSettingsDialog(
    tile: Tile,
    gridColumns: Int,
    accentColor: Color,
    onDismiss: () -> Unit,
    onSetSpans: (columnSpan: Int, rowSpan: Int) -> Unit,
    onToggleLiveTile: () -> Unit
) {
    var widthSlider by remember { mutableFloatStateOf(tile.columnSpan.toFloat()) }
    var heightSlider by remember { mutableFloatStateOf(tile.rowSpan.toFloat()) }

    // 4+ columns wide → minimum 2 rows high
    val minHeight = if (widthSlider.roundToInt() >= 4) 2f else 1f
    LaunchedEffect(minHeight) {
        if (heightSlider < minHeight) {
            heightSlider = minHeight
            onSetSpans(widthSlider.roundToInt(), heightSlider.roundToInt())
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2A2A2A),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = tile.appName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                // Size preview
                Text(
                    text = "TILE SIZE",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                // Preview box
                val previewWidth = widthSlider.roundToInt()
                val previewHeight = heightSlider.roundToInt()
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width((previewWidth * 40).dp)
                            .height((previewHeight * 25).dp)
                            .background(accentColor, RoundedCornerShape(2.dp))
                    ) {
                        Text(
                            text = "${previewWidth}×${previewHeight}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Height slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Height",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${heightSlider.roundToInt()} row${if (heightSlider.roundToInt() > 1) "s" else ""}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = heightSlider.coerceAtLeast(minHeight),
                        onValueChange = { heightSlider = it.coerceAtLeast(minHeight) },
                        onValueChangeFinished = {
                            onSetSpans(widthSlider.roundToInt(), heightSlider.roundToInt())
                        },
                        valueRange = minHeight..4f,
                        steps = (4 - minHeight.toInt()) - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }

                // Width slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Width",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${widthSlider.roundToInt()} column${if (widthSlider.roundToInt() > 1) "s" else ""}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                    Slider(
                        value = widthSlider,
                        onValueChange = { widthSlider = it },
                        onValueChangeFinished = {
                            onSetSpans(widthSlider.roundToInt(), heightSlider.roundToInt())
                        },
                        valueRange = 1f..gridColumns.toFloat(),
                        steps = gridColumns - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                // Live tile toggle
                Text(
                    text = "LIVE TILE",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (tile.isLiveTile) "Enabled" else "Disabled",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = tile.isLiveTile,
                        onCheckedChange = { onToggleLiveTile() },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = accentColor,
                            checkedThumbColor = Color.White
                        )
                    )
                }

                // Close button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Done", color = Color.White)
                }
            }
        }
    }
}
